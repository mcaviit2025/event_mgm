package com.example.event_mgm.dto.response;

import com.example.event_mgm.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPaymentReviewDto {

    private Long registrationId;
    private Long eventId;
    private String eventName;
    private String studentName;
    private String email;
    private String registrationType;
    private Integer numberOfMembers;
    private BigDecimal amountDue;
    private PaymentStatus paymentStatus;
    private Boolean paymentProofAvailable;
    private LocalDateTime registeredAt;
}
