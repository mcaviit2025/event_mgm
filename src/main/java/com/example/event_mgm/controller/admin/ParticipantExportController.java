
package com.example.event_mgm.controller.admin;

import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.dto.request.ParticipantExportRequestDto;
import com.example.event_mgm.service.export.ParticipantExportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/exports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParticipantExportController {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ParticipantExportService participantExportService;
    private final EventRepository eventRepository;

    @GetMapping("/events")
    public ResponseEntity<List<EventOption>> getEventsForExport() {

        List<EventOption> events = eventRepository
                .findAll(Sort.by(Sort.Direction.DESC, "eventDateTime"))
                .stream()
                .map(event -> new EventOption(
                        event.getId(),
                        event.getEventName(),
                        event.getEventDateTime(),
                        event.getIsActive()
                ))
                .toList();

        return ResponseEntity.ok(events);
    }

    @PostMapping(
            value = "/participants",
            produces = EXCEL_CONTENT_TYPE
    )
    public ResponseEntity<byte[]> exportParticipants(
            @Valid @RequestBody ParticipantExportRequestDto request) {

        byte[] excelFile =
                participantExportService.exportParticipants(request);

        String filename = request.getExportType()
                == ParticipantExportRequestDto.ExportType.EVENT
                ? "event-" + request.getEventId() + "-participants.xlsx"
                : "all-events-participants.xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename)
                                .build()
                                .toString()
                )
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .contentLength(excelFile.length)
                .body(excelFile);
    }

    public record EventOption(
            Long id,
            String eventName,
            java.time.LocalDateTime eventDateTime,
            Boolean isActive
    ) {}
}
