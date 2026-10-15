package com.example.event_mgm.dto.request;

import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.enums.RegistrationMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequestDto {

    @Size(min = 3, max = 100, message = "Event name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-']+$", message = "Event name contains invalid characters")
    private String eventName;

    @Size(min = 3, max = 100, message = "Venue must be between 3 and 100 characters")
    private String venue;

    @FutureOrPresent(message = "Event date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDateTime;

    @FutureOrPresent(message = "Registration deadline cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationDeadline;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @DecimalMin(value = "0.00", message = "Event fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Event fee must have no more than 2 decimal places")
    private BigDecimal eventFee;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 10000, message = "Maximum participants cannot exceed 10000")
    private Integer maxParticipants;

    @Min(value = 1, message = "Maximum participants per group must be at least 1")
    @Max(value = 50, message = "Maximum participants per group cannot exceed 50")
    private Integer maxParticipantsPerGroup;

    private RegistrationMode registrationMode;

    private EventStatus status;

    private Long eventTypeId;

    private Long categoryId;

    private Boolean isActive;

    @AssertTrue(message = "Registration deadline must be before event date and time")
    public boolean isRegistrationDeadlineBeforeEvent() {
        if (eventDateTime == null || registrationDeadline == null) return true;
        return registrationDeadline.isBefore(eventDateTime);
    }

    @AssertTrue(message = "Registration deadline must be at least 2 hours before event start")
    public boolean isRegistrationDeadlineAtLeastTwoHoursBefore() {
        if (eventDateTime == null || registrationDeadline == null) return true;
        return registrationDeadline.plusHours(2).isBefore(eventDateTime)
                || registrationDeadline.plusHours(2).equals(eventDateTime);
    }
}