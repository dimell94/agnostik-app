package com.agnostik.agnostik_app.api;


import com.agnostik.agnostik_app.authentication.AuthenticationService;
import com.agnostik.agnostik_app.dto.AuthenticationRequestDTO;
import com.agnostik.agnostik_app.dto.AuthenticationResponseDTO;
import com.agnostik.agnostik_app.dto.UserRegisterDTO;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "User registration and login endpoints returning JWT")
public class AuthRestController {

    private final AuthenticationService authService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user, logs them in, and returns a JWT plus initial presence join.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered",
                            content = @Content(schema = @Schema(implementation = AuthenticationResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "User already exists",
                            content = @Content(schema = @Schema(example = "{\"code\":\"USER_ALREADY_EXISTS\",\"message\":\"...\"}"))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody UserRegisterDTO dto) {
        AuthenticationResponseDTO response = authService.register(dto);
        presenceService.join(response.getUserId());
        var neighbors = presenceService.getNeighbors(response.getUserId());
        Set<Long> affected = Stream.of(
                        response.getUserId(),
                        neighbors.getLeftUserId(),
                        neighbors.getRightUserId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        snapshotNotifierService.notifyUsers(affected);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate existing user",
            description = "Validates credentials, returns JWT, and joins presence.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authenticated",
                            content = @Content(schema = @Schema(implementation = AuthenticationResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(example = "{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"...\"}")))
            }
    )
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @ RequestBody AuthenticationRequestDTO dto) {
        AuthenticationResponseDTO response = authService.login(dto);
        presenceService.join(response.getUserId());

        var neighbors = presenceService.getNeighbors(response.getUserId());
        Set<Long> affected = Stream.of(
                        response.getUserId(),
                        neighbors.getLeftUserId(),
                        neighbors.getRightUserId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        snapshotNotifierService.notifyUsers(affected);
        return ResponseEntity.ok(response);
    }
}
