package com.example.event_mgm.dto;

import com.example.event_mgm.enums.PaymentStatus;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoloRegistrationResponseDto {

    private Long registrationId;

    private Long eventId;
    private String eventName;

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;

    private String collegeName;
    private String course;

    private String utrNo;

    private PaymentStatus paymentStatus;

    private LocalDateTime registeredAt;
}