package com.example.event_mgm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Event Management System API",
                version = "1.0.0",
                description = "API documentation for Event Management System",
                contact = @Contact(
                        name = "Event Management Team",
                        email = "support@eventmgm.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://springdoc.org"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080/api",
                        description = "Local Development Server"
                )
        },
        security = {
                @SecurityRequirement(name = "Bearer Authentication")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT token with Bearer prefix. Example: Bearer eyJhbGciOiJIUzUxMiJ9...",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}