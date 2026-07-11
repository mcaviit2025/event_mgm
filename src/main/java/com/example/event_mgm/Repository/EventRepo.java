package com.example.event_mgm.Repository;

import com.example.event_mgm.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event,Long> {
}
