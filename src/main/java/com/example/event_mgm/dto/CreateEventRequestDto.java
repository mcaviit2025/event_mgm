package com.example.event_mgm.dto;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequestDto {

    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 50, message = "Event name must be between 3 and 50 characters")
    private String eventName;

    @NotBlank(message = "Venue is required")
    @Size(max = 50, message = "Venue cannot exceed 50 characters")
    private String venue;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date cannot be in the past")
    private LocalDate eventDate;

    @NotNull(message = "Registration due date is required")
    @FutureOrPresent(message = "Registration due date cannot be in the past")
    private LocalDate registrationDueDate;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Maximum participants is required")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maxParticipants;

    @NotNull(message = "Maximum participants per group is required")
    @Min(value = 1, message = "Maximum participants per group must be at least 1")
    private Integer maxParticipantPerGroup;

    @NotNull(message = "Category is required")
    private String categoryName;

    @NotNull(message = "Event type is required")
    private String eventType;
}