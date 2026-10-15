package com.example.event_mgm.dto.response;

import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.enums.RegistrationMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {

    private Long id;
    private String eventName;
    private String venue;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registrationDeadline;

    private String description;
    private BigDecimal eventFee;
    private Integer maxParticipants;
    private Integer maxParticipantsPerGroup;
    private Integer currentParticipants;
    private EventStatus status;
    private RegistrationMode registrationMode;

    // Calculated fields
    private Integer availableSlots;
    private Boolean isFull;
    private Boolean isRegistrationOpen;

    // Related entities
    private EventTypeResponseDto eventType;
    private CategoryResponseDto category;
    private UserResponseDto createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Boolean isActive;
}