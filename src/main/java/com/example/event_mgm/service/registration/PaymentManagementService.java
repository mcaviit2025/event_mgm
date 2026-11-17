package com.example.event_mgm.service.registration;

import com.example.event_mgm.dto.request.PaymentReviewRequestDto;
import com.example.event_mgm.dto.response.AdminPaymentReviewDto;
import com.example.event_mgm.dto.response.CollectionsResponseDto;
import com.example.event_mgm.dto.response.EventCollectionDto;
import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.exceptions.InvalidData;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.repository.RegistrationRepository;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentManagementService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public List<AdminPaymentReviewDto> getPayments() {
        return registrationRepository.findByIsActiveTrueOrderByRegisteredAtDesc()
                .stream()
                .map(this::toPaymentReviewDto)
                .toList();
    }

    public CollectionsResponseDto getCollections() {
        List<Event> events = eventRepository.findAll();
        Map<Long, List<Registration>> registrationsByEvent =
                registrationRepository.findByIsActiveTrueOrderByRegisteredAtDesc().stream()
                        .collect(Collectors.groupingBy(registration -> registration.getEvent().getId()));

        List<EventCollectionDto> eventCollections = events.stream()
                .sorted((first, second) -> first.getEventName().compareToIgnoreCase(second.getEventName()))
                .map(event -> toEventCollectionDto(
                        event,
                        registrationsByEvent.getOrDefault(event.getId(), List.of())))
                .toList();

        return CollectionsResponseDto.builder()
                .registrationCount(eventCollections.stream().mapToLong(EventCollectionDto::getRegistrationCount).sum())
                .verifiedPaymentCount(eventCollections.stream().mapToLong(EventCollectionDto::getVerifiedPaymentCount).sum())
                .amountCollected(eventCollections.stream()
                        .map(EventCollectionDto::getAmountCollected)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .events(eventCollections)
                .build();
    }

    @Transactional
    public AdminPaymentReviewDto reviewPayment(Long registrationId, PaymentReviewRequestDto request) {
        PaymentStatus requestedStatus = request.getPaymentStatus();
        if (requestedStatus != PaymentStatus.VERIFIED && requestedStatus != PaymentStatus.REJECTED) {
            throw new InvalidData("Payment can only be verified or rejected");
        }

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found with ID: " + registrationId));
        if (!Boolean.TRUE.equals(registration.getIsActive())) {
            throw new InvalidData("Inactive registrations cannot be reviewed");
        }
        if (registration.getEvent().getEventFee() == null
                || registration.getEvent().getEventFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidData("Free event registrations do not require payment review");
        }
        if (registration.getPaymentProofPath() == null || registration.getPaymentProofPath().isBlank()) {
            throw new InvalidData("A payment screenshot is required before reviewing this payment");
        }

        registration.setPaymentStatus(requestedStatus.name());
        return toPaymentReviewDto(registrationRepository.save(registration));
    }

    public String getPaymentProofPath(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found with ID: " + registrationId));
        if (registration.getPaymentProofPath() == null || registration.getPaymentProofPath().isBlank()) {
            throw new ResourceNotFoundException("Payment screenshot not found for this registration");
        }
        return registration.getPaymentProofPath();
    }

    private EventCollectionDto toEventCollectionDto(Event event, List<Registration> registrations) {
        long verified = registrations.stream()
                .filter(registration -> PaymentStatus.VERIFIED.name().equals(registration.getPaymentStatus()))
                .count();
        long pending = registrations.stream()
                .filter(registration -> PaymentStatus.PENDING.name().equals(registration.getPaymentStatus()))
                .count();
        BigDecimal amountCollected = registrations.stream()
                .filter(registration -> PaymentStatus.VERIFIED.name().equals(registration.getPaymentStatus()))
                .map(Registration::getAmountDue)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = event.getEventFee() == null ? BigDecimal.ZERO : event.getEventFee();

        return EventCollectionDto.builder()
                .eventId(event.getId())
                .eventName(event.getEventName())
                .eventFee(fee)
                .registrationCount(registrations.size())
                .verifiedPaymentCount(verified)
                .pendingPaymentCount(pending)
                .amountCollected(amountCollected)
                .build();
    }

    private AdminPaymentReviewDto toPaymentReviewDto(Registration registration) {
        String name = registration.getFirstName()
                + (registration.getMiddleName() == null || registration.getMiddleName().isBlank()
                ? " " : " " + registration.getMiddleName() + " ")
                + registration.getLastName();
        Event event = registration.getEvent();
        return AdminPaymentReviewDto.builder()
                .registrationId(registration.getId())
                .eventId(event.getId())
                .eventName(event.getEventName())
                .studentName(name)
                .email(registration.getEmail())
                .registrationType(registration.getRegistrationType())
                .numberOfMembers(registration.getNumberOfMembers())
                .amountDue(registration.getAmountDue() == null ? BigDecimal.ZERO : registration.getAmountDue())
                .paymentStatus(PaymentStatus.valueOf(registration.getPaymentStatus()))
                .paymentProofAvailable(registration.getPaymentProofPath() != null
                        && !registration.getPaymentProofPath().isBlank())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}
