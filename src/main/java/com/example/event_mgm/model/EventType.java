package com.example.event_mgm.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_types", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "eventType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();
}