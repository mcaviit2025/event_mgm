package com.example.event_mgm.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetEventResponseDto {

    private Long id;

    private String eventName;

    private String venue;

    private LocalDate eventDate;

    private LocalDate registrationDueDate;

    private String description;

    private Integer maxParticipants;

    private Integer maxParticipantPerGroup;

    private Boolean active;

    private String categoryName;

    private String eventType;
}