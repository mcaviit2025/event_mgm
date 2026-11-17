package com.example.event_mgm.controller.event;

import com.example.event_mgm.dto.request.CreateEventRequestDto;
import com.example.event_mgm.dto.request.UpdateEventRequestDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.EventResponseDto;
import com.example.event_mgm.security.CustomUserDetails;

import com.example.event_mgm.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class EventController {

    private final EventService eventService;



    @PostMapping
    @Operation(summary = "Create a new event")
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(
            @Valid @RequestBody CreateEventRequestDto request) {

        log.info("Creating new event: {}", request.getEventName());

        Long adminId = getCurrentAdminId();
        EventResponseDto response = eventService.createEvent(request, adminId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }



    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEventById(
            @PathVariable Long id) {

        log.info("Fetching event with ID: {}", id);

        EventResponseDto response = eventService.getEventById(id);

        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all events")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEvents() {

        log.info("Fetching all events");

        List<EventResponseDto> response = eventService.getAllEvents();

        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }



    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequestDto request) {

        log.info("Updating event with ID: {}", id);

        Long adminId = getCurrentAdminId();
        EventResponseDto response = eventService.updateEvent(id, request, adminId);

        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event and all associated registrations and participants")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id) {

        log.info("Deleting event with ID: {}", id);

        eventService.deleteEvent(id);

        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }



    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an event")
    public ResponseEntity<ApiResponse<Void>> activateEvent(
            @PathVariable Long id) {

        log.info("Activating event with ID: {}", id);

        eventService.activateEvent(id);

        return ResponseEntity.ok(ApiResponse.success("Event activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an event")
    public ResponseEntity<ApiResponse<Void>> deactivateEvent(
            @PathVariable Long id) {

        log.info("Deactivating event with ID: {}", id);

        eventService.deactivateEvent(id);

        return ResponseEntity.ok(ApiResponse.success("Event deactivated successfully"));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}