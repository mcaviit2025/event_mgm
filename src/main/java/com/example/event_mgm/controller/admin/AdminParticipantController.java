package com.example.event_mgm.controller.admin;

import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.service.registration.ParticipantExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/participants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminParticipantController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ParticipantExportService participantExportService;

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportParticipants(
            @RequestParam(required = false) List<Long> eventIds,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) RegistrationStatus registrationStatus,
            @RequestParam(defaultValue = "true") boolean includeTeamMembers) {
        String filename = "participants-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(participantExportService.export(
                        eventIds, paymentStatus, registrationStatus, includeTeamMembers));
    }
}
