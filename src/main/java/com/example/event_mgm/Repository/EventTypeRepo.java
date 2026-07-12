package com.example.event_mgm.Repository;

import com.example.event_mgm.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventTypeRepo extends JpaRepository<EventType,Long> {

    Optional<EventType> findByEventType(String Name);
}
