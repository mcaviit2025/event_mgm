
package com.example.event_mgm.service.export;

import com.example.event_mgm.dto.request.ParticipantExportRequestDto;

public interface ParticipantExportService {

    byte[] exportParticipants(
            ParticipantExportRequestDto request
    );
}
