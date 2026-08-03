package com.example.event_mgm.controller.auth;

import com.example.event_mgm.dto.request.AdminLoginRequestDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.AuthResponseDto;
import com.example.event_mgm.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Admin Login", description = "Authenticate admin user")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody AdminLoginRequestDto request) {

        log.info("Login request for user: {}", request.getUsername());

        AuthResponseDto response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid refresh token format");
        }

        String refreshToken = authorizationHeader.substring(7);
        AuthResponseDto response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate current session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            authService.logout(token);
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}