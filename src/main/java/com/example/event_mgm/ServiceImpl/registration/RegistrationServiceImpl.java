package com.example.event_mgm.ServiceImpl.registration;

import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.repository.RegistrationRepository;
import com.example.event_mgm.Repository.repository.TeamMemberRepository;
import com.example.event_mgm.dto.request.GroupRegistrationRequestDto;
import com.example.event_mgm.dto.request.ParticipantDetailsDto;
import com.example.event_mgm.dto.request.SoloRegistrationRequestDto;
import com.example.event_mgm.dto.response.RegistrationResponseDto;
import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationMode;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.enums.RegistrationType;
import com.example.event_mgm.exceptions.DuplicateResourceException;
import com.example.event_mgm.exceptions.InvalidData;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.TeamMember;

import com.example.event_mgm.service.registration.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EventRepository eventRepository;

    // ============================================================
    // SOLO REGISTRATION
    // ============================================================

    @Override
    public RegistrationResponseDto registerSolo(SoloRegistrationRequestDto request) {
        log.info("Processing solo registration for event: {}", request.getEventId());

        // Validate Event
        Event event = validateEvent(request.getEventId(), RegistrationType.SOLO);

        // Validate Solo Registration
        validateSoloRegistration(request, event);

        // Build Registration
        Registration registration = Registration.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .collegeName(request.getCollegeName())
                .course(request.getCourse())
                .utrNo(request.getUtrNo())
                .numberOfMembers(1)
                .registrationType(RegistrationType.SOLO.name())
                .paymentStatus(PaymentStatus.PENDING.name())
                .registrationStatus(RegistrationStatus.PENDING.name())
                .event(event)
                .isActive(true)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);
        log.info("Solo registration completed. ID: {}", savedRegistration.getId());

        return mapToResponseDto(savedRegistration);
    }

    // ============================================================
    // GROUP REGISTRATION
    // ============================================================

    @Override
    public RegistrationResponseDto registerGroup(GroupRegistrationRequestDto request) {
        log.info("Processing group registration for event: {}", request.getEventId());

        // Validate Event
        Event event = validateEvent(request.getEventId(), RegistrationType.GROUP);

        // Validate Group Registration
        validateGroupRegistration(request, event);

        // Build Registration (Leader)
        ParticipantDetailsDto leader = request.getLeader();
        Registration registration = Registration.builder()
                .firstName(leader.getFirstName())
                .middleName(leader.getMiddleName())
                .lastName(leader.getLastName())
                .email(leader.getEmail())
                .phoneNumber(leader.getPhoneNumber())
                .dateOfBirth(leader.getDateOfBirth())
                .collegeName(leader.getCollegeName())
                .course(leader.getCourse())
                .utrNo(request.getUtrNo())
                .numberOfMembers(1 + request.getTeamMembers().size())
                .registrationType(RegistrationType.GROUP.name())
                .paymentStatus(PaymentStatus.PENDING.name())
                .registrationStatus(RegistrationStatus.PENDING.name())
                .event(event)
                .isActive(true)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);

        // Save Team Members
        for (ParticipantDetailsDto member : request.getTeamMembers()) {
            TeamMember teamMember = TeamMember.builder()
                    .firstName(member.getFirstName())
                    .middleName(member.getMiddleName())
                    .lastName(member.getLastName())
                    .email(member.getEmail())
                    .phoneNumber(member.getPhoneNumber())
                    .dateOfBirth(member.getDateOfBirth())
                    .registration(savedRegistration)
                    .isActive(true)
                    .build();
            teamMemberRepository.save(teamMember);
        }

        log.info("Group registration completed. ID: {}", savedRegistration.getId());

        return mapToResponseDto(savedRegistration);
    }

    // ============================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================

    private Event validateEvent(Long eventId, RegistrationType registrationType) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new InvalidData("Event is not open for registration. Current status: " + event.getStatus());
        }

        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new InvalidData("Registration deadline has passed");
        }

        validateEventType(event, registrationType);

        return event;
    }

    private void validateEventType(Event event, RegistrationType registrationType) {
        String eventTypeName = event.getEventType().getName().toUpperCase();

        if (registrationType == RegistrationType.SOLO) {
            if (!"SOLO".equals(eventTypeName)) {
                throw new InvalidData(
                        String.format("This event is for '%s' participation only. Solo registrations are not allowed.",
                                eventTypeName)
                );
            }
        } else if (registrationType == RegistrationType.GROUP) {
            if (!"GROUP".equals(eventTypeName)) {
                throw new InvalidData(
                        String.format("This event is for '%s' participation only. Group registrations are not allowed.",
                                eventTypeName)
                );
            }
        }
    }

    private void validateSoloRegistration(SoloRegistrationRequestDto request, Event event) {
        if (event.getRegistrationMode() == RegistrationMode.GROUP_ONLY) {
            throw new InvalidData("This event only allows group registrations");
        }

        if (registrationRepository.existsByEventIdAndEmail(event.getId(), request.getEmail())) {
            throw new DuplicateResourceException("Email already registered for this event");
        }

        if (event.getCurrentParticipants() + 1 > event.getMaxParticipants()) {
            throw new InvalidData("Event has reached maximum capacity");
        }
    }

    private void validateGroupRegistration(GroupRegistrationRequestDto request, Event event) {
        if (event.getRegistrationMode() == RegistrationMode.SOLO_ONLY) {
            throw new InvalidData("This event only allows solo registrations");
        }

        if (registrationRepository.existsByEventIdAndEmail(event.getId(), request.getLeader().getEmail())) {
            throw new DuplicateResourceException("Leader email already registered for this event");
        }

        int totalMembers = 1 + request.getTeamMembers().size();
        if (totalMembers > event.getMaxParticipantsPerGroup()) {
            throw new InvalidData(
                    String.format("Maximum %d members allowed per group. Current group has %d members",
                            event.getMaxParticipantsPerGroup(), totalMembers)
            );
        }

        if (event.getCurrentParticipants() + totalMembers > event.getMaxParticipants()) {
            throw new InvalidData("Event has reached maximum capacity");
        }

        List<String> emails = request.getTeamMembers().stream()
                .map(ParticipantDetailsDto::getEmail)
                .collect(Collectors.toList());

        if (emails.size() != emails.stream().distinct().count()) {
            throw new InvalidData("Duplicate email found in team members");
        }

        for (ParticipantDetailsDto member : request.getTeamMembers()) {
            if (registrationRepository.existsByEventIdAndEmail(event.getId(), member.getEmail())) {
                throw new DuplicateResourceException(
                        "Team member email '" + member.getEmail() + "' already registered for this event"
                );
            }
        }
    }

    // ============================================================
    // PRIVATE MAPPING METHOD
    // ============================================================

    private RegistrationResponseDto mapToResponseDto(Registration registration) {
        String fullName = registration.getFirstName();
        if (registration.getMiddleName() != null && !registration.getMiddleName().isEmpty()) {
            fullName += " " + registration.getMiddleName();
        }
        fullName += " " + registration.getLastName();

        String eventName = registration.getEvent() != null ? registration.getEvent().getEventName() : null;

        String eventType = null;
        if (registration.getEvent() != null && registration.getEvent().getEventType() != null) {
            eventType = registration.getEvent().getEventType().getName();
        }

        return RegistrationResponseDto.builder()
                .id(registration.getId())
                .name(fullName)
                .email(registration.getEmail())
                .eventName(eventName)
                .eventType(eventType)
                .phoneNumber(registration.getPhoneNumber())
                .collegeName(registration.getCollegeName())
                .course(registration.getCourse())
                .build();
    }
}