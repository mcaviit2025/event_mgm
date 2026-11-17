package com.example.event_mgm.service.admin;

import com.example.event_mgm.dto.request.CreateEventTypeRequestDto;
import com.example.event_mgm.dto.request.UpdateEventTypeRequestDto;
import com.example.event_mgm.dto.response.EventTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventTypeService {
    EventTypeResponseDto createEventType(CreateEventTypeRequestDto request);
    EventTypeResponseDto getEventTypeById(Long id);
//  Page<EventTypeResponseDto> getAllEventTypes(Pageable pageable, String search);
    List<EventTypeResponseDto> getAllEventTypes();
    EventTypeResponseDto updateEventType(Long id, UpdateEventTypeRequestDto request);
    void deleteEventType(Long id);
    void activateEventType(Long id);
    void deactivateEventType(Long id);
}

