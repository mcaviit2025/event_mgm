package com.example.event_mgm.service.auth;

import com.example.event_mgm.dto.request.AdminLoginRequestDto;
import com.example.event_mgm.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(AdminLoginRequestDto request);
    AuthResponseDto refreshToken(String refreshToken);
    void logout(String token);
}