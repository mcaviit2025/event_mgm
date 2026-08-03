package com.example.event_mgm.ServiceImpl.auth;

import com.example.event_mgm.Repository.role.UserRepository;
import com.example.event_mgm.dto.request.AdminLoginRequestDto;
import com.example.event_mgm.dto.response.AuthResponseDto;
import com.example.event_mgm.dto.response.RoleResponseDto;
import com.example.event_mgm.dto.response.UserResponseDto;
import com.example.event_mgm.exceptions.InvalidCredentialsException;
import com.example.event_mgm.model.User;

import com.example.event_mgm.security.CustomUserDetails;
import com.example.event_mgm.security.JwtTokenProvider;
import com.example.event_mgm.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponseDto login(AdminLoginRequestDto request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = tokenProvider.generateToken(userDetails);
            String refreshToken = tokenProvider.generateRefreshToken(userDetails);

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponseDto.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.extractExpiration(accessToken).getTime() - System.currentTimeMillis())
                    .user(mapToUserResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            if (tokenProvider.isTokenExpired(refreshToken)) {
                throw new IllegalArgumentException("Refresh token has expired");
            }

            String username = tokenProvider.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!tokenProvider.validateToken(refreshToken, userDetails)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            User user = customUserDetails.getUser();

            // Generate new access token
            String newAccessToken = tokenProvider.generateToken(customUserDetails);

            log.info("Token refreshed for user: {}", user.getUsername());

            return AuthResponseDto.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.extractExpiration(newAccessToken).getTime() - System.currentTimeMillis())
                    .user(mapToUserResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    @Override
    public void logout(String token) {
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }

    private UserResponseDto mapToUserResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .isEnabled(user.getIsEnabled())
                .lastLogin(user.getLastLogin())
                .role(RoleResponseDto.builder()
                        .id(user.getRole().getId())
                        .name(user.getRole().getName())
                        .description(user.getRole().getDescription())
                        .isActive(user.getRole().getIsActive())
                        .build())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}