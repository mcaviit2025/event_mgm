package com.example.event_mgm.dto.request;

import com.example.event_mgm.enums.RegistrationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoloRegistrationRequestDto {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "College name is required")
    @Size(min = 3, max = 100, message = "College name must be between 3 and 100 characters")
    private String collegeName;

    @NotBlank(message = "Course is required")
    @Size(min = 2, max = 50, message = "Course must be between 2 and 50 characters")
    private String course;

    @NotBlank(message = "UTR number is required")
    @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "UTR number should be 10-20 alphanumeric characters")
    private String utrNo;

}