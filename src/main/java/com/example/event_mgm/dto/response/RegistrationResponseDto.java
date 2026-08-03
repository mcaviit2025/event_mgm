package com.example.event_mgm.dto.response;

import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.enums.RegistrationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDto {

    private Long id;
    private String name;
    private String email;
    private String eventName;
    private String eventType;
    private String phoneNumber;
    private String collegeName;
    private String course;










}