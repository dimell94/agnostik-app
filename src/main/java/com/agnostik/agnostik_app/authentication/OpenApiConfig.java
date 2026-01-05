package com.agnostik.agnostik_app.authentication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Agnostik API",
        version = "v1",
        description = "REST endpoints for auth, snapshot, and presence commands. State changes are delivered via WebSocket events; see [WebSocket docs](/docs/websocket.html)"
    ),
    externalDocs = @ExternalDocumentation(
        description = "WebSocket docs (AsyncAPI)",
        url = "/docs/websocket.html"
    ),
    servers = { @Server(url = "/") },
    security = { @SecurityRequirement(name = "bearer-jwt") }
)
@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT returned by /api/auth/login"
)
public class OpenApiConfig { }
