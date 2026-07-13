package com.example.event_mgm.model;

import com.example.event_mgm.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "registrations")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Registration  extends  BaseEntity{

    @Column(nullable = false,length = 50)
    private String firstName;

    @Column(nullable = false,length = 50)
    private String middleName;

    @Column(nullable = false,length = 50)
    private String lastName;

    @Column(nullable = false,length = 50)
    private String email;

    @Column(nullable = false,length = 50)
    private String collegeName;

    @Column(nullable = false,length = 50)
    private String course;

    @Column(nullable = false,length = 50)
    private String utrNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;


}
