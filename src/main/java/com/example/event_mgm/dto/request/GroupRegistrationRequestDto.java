package com.example.event_mgm.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRegistrationRequestDto {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Valid
    @NotNull(message = "Leader details are required")
    private ParticipantDetailsDto leader;

    @Valid
    @NotEmpty(message = "At least one team member is required")
    private List<ParticipantDetailsDto> teamMembers;

    // Custom validation for group size
    @AssertTrue(message = "Group must have at least 2 members (leader + 1 team member)")
    public boolean isGroupSizeValid() {
        return teamMembers != null && teamMembers.size() >= 1;
    }

    // Custom validation for duplicate emails
    @AssertTrue(message = "Duplicate email found in team members")
    public boolean isTeamMemberEmailsUnique() {
        if (teamMembers == null || teamMembers.isEmpty()) {
            return true;
        }
        long distinctCount = teamMembers.stream()
                .map(ParticipantDetailsDto::getEmail)
                .distinct()
                .count();
        return distinctCount == teamMembers.size();
    }
}