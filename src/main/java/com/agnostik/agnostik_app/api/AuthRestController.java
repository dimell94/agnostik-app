package com.agnostik.agnostik_app.api;


import com.agnostik.agnostik_app.authentication.AuthenticationService;
import com.agnostik.agnostik_app.dto.AuthenticationRequestDTO;
import com.agnostik.agnostik_app.dto.AuthenticationResponseDTO;
import com.agnostik.agnostik_app.dto.UserRegisterDTO;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
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
public class AuthRestController {

    private final AuthenticationService authService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/register")
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
