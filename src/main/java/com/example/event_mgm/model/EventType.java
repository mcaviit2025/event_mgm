package com.example.event_mgm.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events_type")
public class EventType extends  BaseEntity{

    @Column(name = "event_type",nullable = false,length = 20)
    private  String  eventType;

    @OneToMany(mappedBy = "eventType",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Event> events = new ArrayList<>();
}
