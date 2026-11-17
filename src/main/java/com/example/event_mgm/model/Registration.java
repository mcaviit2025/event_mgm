package com.example.event_mgm.model;

import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.enums.RegistrationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {

    //

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, length = 100)
    @Email
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "college_name", nullable = false, length = 100)
    private String collegeName;

    @Column(name = "course", nullable = false, length = 50)
    private String course;

    // ===== PAYMENT DETAILS =====

    @Column(name = "payment_proof_path", length = 500)
    private String paymentProofPath;

    @Column(name = "amount_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "number_of_members", nullable = false)
    @Builder.Default
    private Integer numberOfMembers = 1;

    // ===== STATUS =====


    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private String paymentStatus = PaymentStatus.PENDING.name();


    @Column(name = "registration_status", nullable = false)
    @Builder.Default
    private String registrationStatus = RegistrationStatus.PENDING.name();


    @Column(name = "registration_type", nullable = false)
    private String registrationType;

    @Column(name = "unique_code", length = 50)
    private String uniqueCode;

    // ===== TIMESTAMPS =====

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;



    // ===== RELATIONSHIPS =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;



    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMember> teamMembers = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
