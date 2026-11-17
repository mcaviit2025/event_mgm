
package com.example.event_mgm.dto.request;

import com.example.event_mgm.enums.ParticipantExportField;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantExportRequestDto {

    @NotNull(message = "Export type is required")
    private ExportType exportType;

    // Required only for EVENT export
    private Long eventId;

    @NotEmpty(message = "Select at least one export field")
    private List<ParticipantExportField> selectedFields;

    public enum ExportType {
        EVENT,
        ALL
    }
}
