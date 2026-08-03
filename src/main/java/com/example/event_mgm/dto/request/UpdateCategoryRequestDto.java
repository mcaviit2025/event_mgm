package com.example.event_mgm.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCategoryRequestDto {

    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Category name contains invalid characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private Boolean isActive;
}