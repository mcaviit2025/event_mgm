package com.example.event_mgm.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}