package com.example.event_mgm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events_type")
@Getter
@Setter
public class EventType extends  BaseEntity{

    @Column(name = "event_type",nullable = false,length = 20)
    private  String  eventType;

    @OneToMany(mappedBy = "eventType",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Event> events = new ArrayList<>();
}
