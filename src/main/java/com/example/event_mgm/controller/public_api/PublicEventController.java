package com.example.event_mgm.controller.public_api;

import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.EventResponseDto;
import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Public Events", description = "Public APIs for browsing available events")
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get active events")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAvailableEvents() {
        log.info("Fetching public event listings");
        List<EventResponseDto> events = eventService.getAllEvents().stream()
                .filter(event -> Boolean.TRUE.equals(event.getIsActive()))
                .filter(event -> event.getStatus() == EventStatus.OPEN)
                .filter(event -> Boolean.TRUE.equals(event.getIsRegistrationOpen()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }
}
