package com.example.event_mgm.ServiceImpl.event;

import com.example.event_mgm.Repository.admin.CategoryRepository;
import com.example.event_mgm.Repository.admin.EventTypeRepository;
import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.role.UserRepository;
import com.example.event_mgm.dto.request.CreateEventRequestDto;
import com.example.event_mgm.dto.request.UpdateEventRequestDto;
import com.example.event_mgm.dto.response.*;
import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.enums.RegistrationMode;
import com.example.event_mgm.exceptions.DuplicateResourceException;
import com.example.event_mgm.exceptions.InvalidData;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Category;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.EventType;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.User;

import com.example.event_mgm.service.event.EventService;
import com.example.event_mgm.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;



    @Override
    public EventResponseDto createEvent(CreateEventRequestDto request, Long adminId) {
        log.info("Creating new event: {}", request.getEventName());

        // 1. Check duplicate event name
        if (eventRepository.existsByEventNameAndIsActiveTrue(request.getEventName())) {
            throw new DuplicateResourceException("Event with name '" + request.getEventName() + "' already exists");
        }

        // 2. Validate registration mode rules
        validateEventRules(request);

        // 3. Fetch related entities
        EventType eventType = eventTypeRepository.findById(request.getEventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Event type not found with ID: " + request.getEventTypeId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminId));

        // 4. Build Event entity
        Event event = Event.builder()
                .eventName(request.getEventName().trim())
                .venue(request.getVenue().trim())
                .eventDateTime(request.getEventDateTime())
                .registrationDeadline(request.getRegistrationDeadline())
                .description(request.getDescription().trim())
                .eventFee(request.getEventFee())
                .maxParticipants(request.getMaxParticipants())
                .maxParticipantsPerGroup(request.getMaxParticipantsPerGroup())
                .currentParticipants(0)
                .status(EventStatus.OPEN)
                .registrationMode(request.getRegistrationMode())
                .eventType(eventType)
                .category(category)
                .createdBy(admin)
                .isActive(true)
                .build();

        // 5. Save to database
        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());

        // 6. Return response
        return mapToResponseDto(savedEvent);
    }



    @Override
    public EventResponseDto getEventById(Long id) {
        log.info("Fetching event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        return mapToResponseDto(event);
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        log.info("Fetching all events");

        List<Event> events = eventRepository.findByIsActiveTrue();

        return events.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }



    @Override
    public EventResponseDto updateEvent(Long id, UpdateEventRequestDto request, Long adminId) {
        log.info("Updating event with ID: {}", id);

        // 1. Fetch existing event
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        // 2. Check if event is completed or cancelled
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidData("Cannot update a " + event.getStatus() + " event");
        }

        // 3. Update fields
        if (request.getEventName() != null) {
            if (!request.getEventName().equals(event.getEventName()) &&
                    eventRepository.existsByEventNameAndIsActiveTrue(request.getEventName())) {
                throw new DuplicateResourceException("Event with name '" + request.getEventName() + "' already exists");
            }
            event.setEventName(request.getEventName().trim());
        }

        if (request.getVenue() != null) {
            event.setVenue(request.getVenue().trim());
        }

        if (request.getEventDateTime() != null) {
            if (request.getEventDateTime().isBefore(LocalDateTime.now())) {
                throw new InvalidData("Event date cannot be in the past");
            }
            event.setEventDateTime(request.getEventDateTime());
        }

        if (request.getRegistrationDeadline() != null) {
            if (request.getRegistrationDeadline().isBefore(LocalDateTime.now())) {
                throw new InvalidData("Registration deadline cannot be in the past");
            }
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription().trim());
        }

        if (request.getEventFee() != null) {
            event.setEventFee(request.getEventFee());
        }

        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }

        if (request.getMaxParticipantsPerGroup() != null) {
            event.setMaxParticipantsPerGroup(request.getMaxParticipantsPerGroup());
        }

        if (request.getRegistrationMode() != null) {
            event.setRegistrationMode(request.getRegistrationMode());
        }

        if (request.getEventTypeId() != null) {
            EventType eventType = eventTypeRepository.findById(request.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event type not found"));
            event.setEventType(eventType);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            event.setCategory(category);
        }

        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        if (request.getIsActive() != null) {
            event.setIsActive(request.getIsActive());
        }

        // 4. Validate updated rules
        validateEventRulesForUpdate(event);

        // 5. Save to database
        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully with ID: {}", updatedEvent.getId());

        // 6. Return response
        return mapToResponseDto(updatedEvent);
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        List<String> paymentProofPaths = event.getRegistrations() == null
                ? List.of()
                : event.getRegistrations().stream()
                .map(Registration::getPaymentProofPath)
                .filter(path -> path != null && !path.isBlank())
                .toList();

        eventRepository.delete(event);
        deletePaymentProofsAfterCommit(paymentProofPaths);
        log.info("Event deleted successfully with ID: {}", id);
    }

    private void deletePaymentProofsAfterCommit(List<String> proofPaths) {
        if (proofPaths.isEmpty()) {
            return;
        }
        Runnable deleteFiles = () -> proofPaths.forEach(fileStorageService::deleteFile);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteFiles.run();
                }
            });
        } else {
            deleteFiles.run();
        }
    }

    // ============================================================
    // ACTIVATE / DEACTIVATE
    // ============================================================

    @Override
    public void activateEvent(Long id) {
        log.info("Activating event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        event.setIsActive(true);
        eventRepository.save(event);
        log.info("Event activated successfully with ID: {}", id);
    }

    @Override
    public void deactivateEvent(Long id) {
        log.info("Deactivating event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));

        event.setIsActive(false);
        eventRepository.save(event);
        log.info("Event deactivated successfully with ID: {}", id);
    }

    // ============================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================
// helper method
    private void validateEventRules(CreateEventRequestDto request) {

        if (request.getRegistrationMode() == RegistrationMode.SOLO_ONLY) {
            if (request.getMaxParticipantsPerGroup() != 1) {
                throw new InvalidData("For SOLO_ONLY events, max participants per group must be 1");
            }
        }


        if (request.getRegistrationMode() == RegistrationMode.GROUP_ONLY) {
            if (request.getMaxParticipantsPerGroup() <= 1) {
                throw new InvalidData("For GROUP_ONLY events, max participants per group must be greater than 1");
            }
        }

        // Max participants per group cannot exceed max participants
        if (request.getMaxParticipantsPerGroup() > request.getMaxParticipants()) {
            throw new InvalidData("Max participants per group cannot exceed max participants");
        }

        // Registration deadline must be before event date
        if (!request.getRegistrationDeadline().isBefore(request.getEventDateTime())) {
            throw new InvalidData("Registration deadline must be before event date and time");
        }

        // Registration deadline must be at least 2 hours before event
        if (request.getRegistrationDeadline().plusHours(2).isAfter(request.getEventDateTime())) {
            throw new InvalidData("Registration deadline must be at least 2 hours before event start");
        }
    }

    private void validateEventRulesForUpdate(Event event) {
        // SOLO_ONLY validation
        if (event.getRegistrationMode() == RegistrationMode.SOLO_ONLY) {
            if (event.getMaxParticipantsPerGroup() != 1) {
                throw new InvalidData("For SOLO_ONLY events, max participants per group must be 1");
            }
        }

        // GROUP_ONLY validation
        if (event.getRegistrationMode() == RegistrationMode.GROUP_ONLY) {
            if (event.getMaxParticipantsPerGroup() <= 1) {
                throw new InvalidData("For GROUP_ONLY events, max participants per group must be greater than 1");
            }
        }

        // Max participants per group cannot exceed max participants
        if (event.getMaxParticipantsPerGroup() > event.getMaxParticipants()) {
            throw new InvalidData("Max participants per group cannot exceed max participants");
        }

        // Registration deadline must be before event date
        if (!event.getRegistrationDeadline().isBefore(event.getEventDateTime())) {
            throw new InvalidData("Registration deadline must be before event date and time");
        }
    }

    // ============================================================
    // PRIVATE MAPPING METHODS
    // ============================================================

    private EventResponseDto mapToResponseDto(Event event) {
        // Calculate fields
        int availableSlots = event.getMaxParticipants() - event.getCurrentParticipants();
        boolean isFull = availableSlots <= 0;
        boolean isRegistrationOpen = event.getStatus() == EventStatus.OPEN &&
                LocalDateTime.now().isBefore(event.getRegistrationDeadline()) &&
                !isFull;

        // Build response using builder
        EventResponseDto.EventResponseDtoBuilder builder = EventResponseDto.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .venue(event.getVenue())
                .eventDateTime(event.getEventDateTime())
                .registrationDeadline(event.getRegistrationDeadline())
                .description(event.getDescription())
                .eventFee(event.getEventFee())
                .maxParticipants(event.getMaxParticipants())
                .maxParticipantsPerGroup(event.getMaxParticipantsPerGroup())
                .currentParticipants(event.getCurrentParticipants())
                .status(event.getStatus())
                .registrationMode(event.getRegistrationMode())
                .availableSlots(Math.max(0, availableSlots))
                .isFull(isFull)
                .isRegistrationOpen(isRegistrationOpen)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .isActive(event.getIsActive());

        // Map EventType
        if (event.getEventType() != null) {
            builder.eventType(EventTypeResponseDto.builder()
                    .id(event.getEventType().getId())
                    .name(event.getEventType().getName())
                    .description(event.getEventType().getDescription())
                    .isActive(event.getEventType().getIsActive())
                    .build());
        }

        // Map Category
        if (event.getCategory() != null) {
            builder.category(CategoryResponseDto.builder()
                    .id(event.getCategory().getId())
                    .name(event.getCategory().getName())
                    .description(event.getCategory().getDescription())
                    .isActive(event.getCategory().getIsActive())
                    .build());
        }

        // Map CreatedBy (Admin)
        if (event.getCreatedBy() != null) {
            builder.createdBy(UserResponseDto.builder()
                    .id(event.getCreatedBy().getId())
                    .username(event.getCreatedBy().getUsername())
                    .email(event.getCreatedBy().getEmail())
                    .fullName(event.getCreatedBy().getFullName())
                    .build());
        }

        return builder.build();
    }
}