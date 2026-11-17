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

    List<Event> findByIdInAndIsActiveTrueOrderByEventNameAsc(List<Long> ids);

    @Modifying
    @Query("""
            UPDATE Event e
            SET e.currentParticipants = e.currentParticipants + :participantCount
            WHERE e.id = :eventId
              AND e.isActive = true
              AND e.status = :status
              AND e.currentParticipants + :participantCount <= e.maxParticipants
            """)
    int reserveParticipants(
            @Param("eventId") Long eventId,
            @Param("participantCount") int participantCount,
            @Param("status") EventStatus status);
}