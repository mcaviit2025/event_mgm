package com.example.event_mgm.service.event;
import com.example.event_mgm.dto.CreateEventRequestDto;
import com.example.event_mgm.dto.GetEventResponseDto;

import java.util.List;

public interface EventService {

    GetEventResponseDto createEvent(CreateEventRequestDto request);

    GetEventResponseDto getEventById(Long eventId);

    List<GetEventResponseDto> getAllEvents();

    GetEventResponseDto updateEvent(Long eventId, CreateEventRequestDto request);

    void deleteEvent(Long eventId);

    void activateEvent(Long eventId);

    void deactivateEvent(Long eventId);
}