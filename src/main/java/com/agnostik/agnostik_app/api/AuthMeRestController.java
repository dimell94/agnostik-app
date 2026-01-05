package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "User registration and login endpoints returning JWT")
public class AuthMeRestController {

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns authenticated user info based on the provided JWT.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user returned"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
            }
    )
    public UserReadOnlyDTO me(@AuthenticationPrincipal UserReadOnlyDTO me){

        return me;
    }
}
