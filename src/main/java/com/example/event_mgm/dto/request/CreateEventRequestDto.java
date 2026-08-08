package com.example.event_mgm.dto.request;

import com.example.event_mgm.enums.RegistrationMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequestDto {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 100, message = "Event name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-']+$", message = "Event name contains invalid characters")
    private String eventName;

    @NotBlank(message = "Venue is required")
    @Size(min = 3, max = 100, message = "Venue must be between 3 and 100 characters")
    private String venue;

    @NotNull(message = "Event date and time is required")
    @FutureOrPresent(message = "Event date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDateTime;


    @NotNull(message = "Registration deadline is required")
    @FutureOrPresent(message = "Registration deadline cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime registrationDeadline;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 10000, message = "Maximum participants cannot exceed 10000")
    private Integer maxParticipants;

    @NotNull(message = "Maximum participants per group is required")
    @Min(value = 1, message = "Maximum participants per group must be at least 1")
    @Max(value = 50, message = "Maximum participants per group cannot exceed 50")
    private Integer maxParticipantsPerGroup;

    @NotNull(message = "Registration mode is required")
    private RegistrationMode registrationMode;  // SOLO_ONLY, GROUP_ONLY, BOTH

    @NotNull(message = "Event type ID is required")
    private Long eventTypeId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // ===== CUSTOM VALIDATIONS =====


    @AssertTrue(message = "Registration deadline must be before event date and time")
    public boolean isRegistrationDeadlineBeforeEvent() {
        if (eventDateTime == null || registrationDeadline == null) {
            return true;
        }
        return registrationDeadline.isBefore(eventDateTime);
    }

    // 2. Registration deadline must be at least 2 hours before event
    @AssertTrue(message = "Registration deadline must be at least 2 hours before event start")
    public boolean isRegistrationDeadlineAtLeastTwoHoursBefore() {
        if (eventDateTime == null || registrationDeadline == null) {
            return true;
        }
        return registrationDeadline.plusHours(2).isBefore(eventDateTime)
                || registrationDeadline.plusHours(2).equals(eventDateTime);
    }

    // 3. ✅ SOLO_ONLY → maxParticipantsPerGroup must be exactly 1
    @AssertTrue(message = "For SOLO_ONLY events, max participants per group must be 1")
    public boolean isMaxParticipantsPerGroupValidForSolo() {
        if (registrationMode == RegistrationMode.SOLO_ONLY) {
            return maxParticipantsPerGroup != null && maxParticipantsPerGroup == 1;
        }
        return true;
    }

    // 4. ✅ GROUP_ONLY → maxParticipantsPerGroup must be > 1
    @AssertTrue(message = "For GROUP_ONLY events, max participants per group must be greater than 1")
    public boolean isMaxParticipantsPerGroupValidForGroup() {
        if (registrationMode == RegistrationMode.GROUP_ONLY) {
            return maxParticipantsPerGroup != null && maxParticipantsPerGroup > 1;
        }
        return true;
    }



    // 6. Max participants per group cannot exceed max participants
    @AssertTrue(message = "Max participants per group cannot exceed max participants")
    public boolean isMaxParticipantsPerGroupLessThanMax() {
        if (maxParticipants == null || maxParticipantsPerGroup == null) {
            return true;
        }
        return maxParticipantsPerGroup <= maxParticipants;
    }
}