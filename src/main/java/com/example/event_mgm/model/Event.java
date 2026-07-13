package com.example.event_mgm.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends  BaseEntity {

    @Column(name="event_name",nullable = false,length = 50)
    private String eventName;

    @Column(name="venue",nullable = false,length = 50)
    private String venue;

    @Builder.Default
    private boolean active=true;

    @Column(name="event_date",nullable = false)
    private LocalDate eventDate;

    @Column(name="registration_due_date",nullable = false)
    private  LocalDate registrationDueDate;

    @Column(name="Description",nullable = false)
    private  String description;

    @Column(name = "max_participant_per_group",nullable = false)
    @Min(1)
    private Integer maxParticipantPerGroup;

    @Column(name = "max_particiapant",nullable = false)
    @Min(1)
    private Integer maxParticipants;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name="event_type_id")
    private EventType eventType;


}
