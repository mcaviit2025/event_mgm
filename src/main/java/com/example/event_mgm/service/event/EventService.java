package com.example.event_mgm.service.event;

import com.example.event_mgm.dto.request.CreateEventRequestDto;
import com.example.event_mgm.dto.request.UpdateEventRequestDto;
import com.example.event_mgm.dto.response.EventResponseDto;

import java.util.List;

public interface EventService {

    // ===== CREATE =====
    EventResponseDto createEvent(CreateEventRequestDto request, Long adminId);

    // ===== READ =====
    EventResponseDto getEventById(Long id);
    List<EventResponseDto> getAllEvents();

    // ===== UPDATE =====
    EventResponseDto updateEvent(Long id, UpdateEventRequestDto request, Long adminId);

    // ===== DELETE =====
    void deleteEvent(Long id);

    // ===== ACTIVATE/DEACTIVATE =====
    void activateEvent(Long id);
    void deactivateEvent(Long id);
}