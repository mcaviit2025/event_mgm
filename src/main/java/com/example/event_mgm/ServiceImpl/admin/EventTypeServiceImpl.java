package com.example.event_mgm.ServiceImpl.admin;

import com.example.event_mgm.Repository.admin.EventTypeRepository;
import com.example.event_mgm.dto.request.CreateEventTypeRequestDto;
import com.example.event_mgm.dto.request.UpdateEventTypeRequestDto;
import com.example.event_mgm.dto.response.EventTypeResponseDto;
import com.example.event_mgm.exceptions.DuplicateResourceException;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.EventType;

import com.example.event_mgm.service.admin.EventTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventTypeServiceImpl implements EventTypeService {

    private final EventTypeRepository eventTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    public EventTypeResponseDto createEventType(CreateEventTypeRequestDto request) {
        log.info("Creating new event type: {}", request.getName());

        if (eventTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Event type with name '" + request.getName() + "' already exists");
        }

        EventType eventType = EventType.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(true)
                .build();

        EventType savedEventType = eventTypeRepository.save(eventType);
        log.info("Event type created successfully with ID: {}", savedEventType.getId());

        return mapToResponseDto(savedEventType);
    }

    @Override
    public EventTypeResponseDto getEventTypeById(Long id) {
        log.info("Fetching event type with ID: {}", id);

        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + id));

        return mapToResponseDto(eventType);
    }

//    @Override
//    public Page<EventTypeResponseDto> getAllEventTypes(Pageable pageable, String search) {
//        log.info("Fetching all event types with search: {}", search);
//
//        Page<EventType> eventTypes;
//        if (search != null && !search.isEmpty()) {
//            eventTypes = eventTypeRepository.findByNameContainingIgnoreCase(search, pageable);
//        } else {
//            eventTypes = eventTypeRepository.findAll(pageable);
//        }
//
//        return eventTypes.map(this::mapToResponseDto);
//    }

    @Override
    public List<EventTypeResponseDto> getAllEventTypes() {
        log.info("Fetching all event types");

        List<EventType> eventTypes = eventTypeRepository.findAll();

        return eventTypes.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventTypeResponseDto updateEventType(Long id, UpdateEventTypeRequestDto request) {
        log.info("Updating event type with ID: {}", id);

        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + id));

        if (request.getName() != null && !request.getName().equals(eventType.getName())) {
            if (eventTypeRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Event type with name '" + request.getName() + "' already exists");
            }
            eventType.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            eventType.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            eventType.setIsActive(request.getIsActive());
        }

        EventType updatedEventType = eventTypeRepository.save(eventType);
        log.info("Event type updated successfully with ID: {}", updatedEventType.getId());

        return mapToResponseDto(updatedEventType);
    }

    @Override
    public void deleteEventType(Long id) {
        log.info("Deleting event type with ID: {}", id);

        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + id));

        if (!eventType.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot delete event type as it has associated events");
        }

        eventTypeRepository.delete(eventType);
        log.info("Event type deleted successfully with ID: {}", id);
    }

    @Override
    public void activateEventType(Long id) {
        log.info("Activating event type with ID: {}", id);

        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + id));

        eventType.setIsActive(true);
        eventTypeRepository.save(eventType);
        log.info("Event type activated successfully with ID: {}", id);
    }

    @Override
    public void deactivateEventType(Long id) {
        log.info("Deactivating event type with ID: {}", id);

        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + id));

        eventType.setIsActive(false);
        eventTypeRepository.save(eventType);
        log.info("Event type deactivated successfully with ID: {}", id);
    }

    private EventTypeResponseDto mapToResponseDto(EventType eventType) {
        return EventTypeResponseDto.builder()
                .id(eventType.getId())
                .name(eventType.getName())
                .description((eventType.getDescription()))
                .isActive(eventType.getIsActive())
                .build();}
}