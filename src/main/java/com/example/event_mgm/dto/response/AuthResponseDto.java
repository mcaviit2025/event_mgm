package com.example.event_mgm.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String token;
    private String tokenType;
    private  String refreshToken;
    private Long expiresIn;
    private UserResponseDto user;
}