package com.example.event_mgm.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean isEnabled;
    private LocalDateTime lastLogin;
    private RoleResponseDto role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}