package com.example.event_mgm.controller.admin;

import com.example.event_mgm.dto.request.CreateEventTypeRequestDto;
import com.example.event_mgm.dto.request.UpdateEventTypeRequestDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.EventTypeResponseDto;
import com.example.event_mgm.service.admin.EventTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/event-types")
@RequiredArgsConstructor
@Tag(name = "Event Type Management", description = "APIs for managing event types")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @PostMapping
    @Operation(summary = "Create a new event type")
    public ResponseEntity<ApiResponse<EventTypeResponseDto>> createEventType(
            @Valid @RequestBody CreateEventTypeRequestDto request) {

        log.info("Creating new event type: {}", request.getName());

        EventTypeResponseDto response = eventTypeService.createEventType(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event type created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event type by ID")
    public ResponseEntity<ApiResponse<EventTypeResponseDto>> getEventTypeById(
            @PathVariable Long id) {

        log.info("Fetching event type with ID: {}", id);

        EventTypeResponseDto response = eventTypeService.getEventTypeById(id);

        return ResponseEntity.ok(ApiResponse.success("Event type retrieved successfully", response));
    }

//    @GetMapping
//    @Operation(summary = "Get all event types with pagination")
//    public ResponseEntity<ApiResponse<Page<EventTypeResponseDto>>> getAllEventTypes(
//            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
//            @RequestParam(required = false) String search) {
//
//        log.info("Fetching all event types with search: {}", search);
//
//        Page<EventTypeResponseDto> response = eventTypeService.getAllEventTypes(pageable, search);
//
//        return ResponseEntity.ok(ApiResponse.success("Event types retrieved successfully", response));
//    }

    @GetMapping
    @Operation(summary = "Get all event types")
    public ResponseEntity<ApiResponse<List<EventTypeResponseDto>>> getAllEventTypes() {

        log.info("Fetching all event types");
        List<EventTypeResponseDto> response = eventTypeService.getAllEventTypes();
        return ResponseEntity.ok(ApiResponse.success("Event types retrieved successfully", response));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event type")
    public ResponseEntity<ApiResponse<EventTypeResponseDto>> updateEventType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventTypeRequestDto request) {

        log.info("Updating event type with ID: {}", id);

        EventTypeResponseDto response = eventTypeService.updateEventType(id, request);

        return ResponseEntity.ok(ApiResponse.success("Event type updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event type")
    public ResponseEntity<ApiResponse<Void>> deleteEventType(
            @PathVariable Long id) {

        log.info("Deleting event type with ID: {}", id);

        eventTypeService.deleteEventType(id);

        return ResponseEntity.ok(ApiResponse.success("Event type deleted successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an event type")
    public ResponseEntity<ApiResponse<Void>> activateEventType(
            @PathVariable Long id) {

        log.info("Activating event type with ID: {}", id);

        eventTypeService.activateEventType(id);

        return ResponseEntity.ok(ApiResponse.success("Event type activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an event type")
    public ResponseEntity<ApiResponse<Void>> deactivateEventType(
            @PathVariable Long id) {

        log.info("Deactivating event type with ID: {}", id);

        eventTypeService.deactivateEventType(id);

        return ResponseEntity.ok(ApiResponse.success("Event type deactivated successfully"));
    }
}