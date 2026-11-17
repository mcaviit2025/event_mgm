
package com.example.event_mgm.model;

import com.example.event_mgm.enums.EventStatus;
import com.example.event_mgm.enums.RegistrationMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName;

    @Column(name = "venue", nullable = false, length = 100)
    private String venue;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(
            name = "event_fee",
            nullable = false,
            precision = 10,
            scale = 2,
            columnDefinition = "DECIMAL(10,2) DEFAULT 0"
    )
    @DecimalMin("0.00")
    @Builder.Default
    private BigDecimal eventFee = BigDecimal.ZERO;

    @Column(name = "max_participants", nullable = false)
    @Min(1)
    private Integer maxParticipants;

    @Column(name = "max_participants_per_group", nullable = false)
    @Min(1)
    private Integer maxParticipantsPerGroup;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_mode", nullable = false)
    private RegistrationMode registrationMode;

    // Category deletion is restricted while events reference it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // EventType deletion is restricted while events reference it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    // Preserve the user who created the event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Event deletion removes all associated registrations
    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
