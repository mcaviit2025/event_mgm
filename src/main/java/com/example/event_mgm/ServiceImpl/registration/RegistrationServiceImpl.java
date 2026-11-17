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
import com.example.event_mgm.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final FileStorageService fileStorageService;

    // ============================================================
    // SOLO REGISTRATION
    // ============================================================

    @Override
    public RegistrationResponseDto registerSolo(
            SoloRegistrationRequestDto request,
            MultipartFile paymentScreenshot) {
        log.info("Processing solo registration for event: {}", request.getEventId());

        // Validate Event
        Event event = validateEvent(request.getEventId());

        // Validate Solo Registration
        validateSoloRegistration(request, event);
        reserveEventCapacity(event, 1);

        // Build Registration
        Registration registration = Registration.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .collegeName(request.getCollegeName())
                .course(request.getCourse())
                .numberOfMembers(1)
                .registrationType(RegistrationType.SOLO.name())
                .paymentStatus(paymentStatusFor(event).name())
                .amountDue(amountDueFor(event))
                .registrationStatus(RegistrationStatus.PENDING.name())
                .event(event)
                .isActive(true)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);
        savePaymentProof(savedRegistration, event, paymentScreenshot);
        log.info("Solo registration completed. ID: {}", savedRegistration.getId());

        return mapToResponseDto(savedRegistration);
    }

    // ============================================================
    // GROUP REGISTRATION
    // ============================================================

    @Override
    public RegistrationResponseDto registerGroup(
            GroupRegistrationRequestDto request,
            MultipartFile paymentScreenshot) {
        log.info("Processing group registration for event: {}", request.getEventId());

        // Validate Event
        Event event = validateEvent(request.getEventId());

        // Validate Group Registration
        validateGroupRegistration(request, event);
        reserveEventCapacity(event, 1 + request.getTeamMembers().size());

        // Build Registration (Leader)
        ParticipantDetailsDto leader = request.getLeader();
        Registration registration = Registration.builder()
                .firstName(leader.getFirstName())
                .middleName(leader.getMiddleName())
                .lastName(leader.getLastName())
                .email(leader.getEmail())
                .phoneNumber(leader.getPhoneNumber())
                .collegeName(leader.getCollegeName())
                .course(leader.getCourse())
                .numberOfMembers(1 + request.getTeamMembers().size())
                .registrationType(RegistrationType.GROUP.name())
                .paymentStatus(paymentStatusFor(event).name())
                .amountDue(amountDueFor(event))
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
                    .registration(savedRegistration)
                    .isActive(true)
                    .build();
            teamMemberRepository.save(teamMember);
        }

        savePaymentProof(savedRegistration, event, paymentScreenshot);
        log.info("Group registration completed. ID: {}", savedRegistration.getId());

        return mapToResponseDto(savedRegistration);
    }

    private PaymentStatus paymentStatusFor(Event event) {
        return amountDueFor(event).compareTo(BigDecimal.ZERO) > 0
                ? PaymentStatus.PENDING
                : PaymentStatus.NOT_REQUIRED;
    }

    private BigDecimal amountDueFor(Event event) {
        return event.getEventFee() == null ? BigDecimal.ZERO : event.getEventFee();
    }

    private void savePaymentProof(
            Registration registration,
            Event event,
            MultipartFile paymentScreenshot) {
        boolean paymentRequired = paymentStatusFor(event) == PaymentStatus.PENDING;
        if (paymentRequired && (paymentScreenshot == null || paymentScreenshot.isEmpty())) {
            throw new InvalidData("Payment screenshot is required for paid events");
        }
        if (paymentScreenshot == null || paymentScreenshot.isEmpty()) {
            return;
        }

        String proofPath = fileStorageService.storePaymentScreenshot(paymentScreenshot, registration.getId());
        try {
            registration.setPaymentProofPath(proofPath);
            registrationRepository.save(registration);
        } catch (RuntimeException exception) {
            fileStorageService.deleteFile(proofPath);
            throw exception;
        }
    }

    // ============================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================

    private Event validateEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new InvalidData("Event is not open for registration. Current status: " + event.getStatus());
        }

        if (!Boolean.TRUE.equals(event.getIsActive())) {
            throw new InvalidData("Event is not available for registration");
        }

        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new InvalidData("Registration deadline has passed");
        }

        return event;
    }

    private void reserveEventCapacity(Event event, int participantCount) {
        int reserved = eventRepository.reserveParticipants(
                event.getId(),
                participantCount,
                EventStatus.OPEN);
        if (reserved != 1) {
            throw new InvalidData("Event has reached maximum capacity or is no longer open for registration");
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