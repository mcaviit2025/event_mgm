package com.example.event_mgm.Repository.event;

import com.example.event_mgm.model.Event;
import com.example.event_mgm.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {



    boolean existsByEventNameAndIsActiveTrue(String eventName);


    List<Event> findByIsActiveTrue();
}