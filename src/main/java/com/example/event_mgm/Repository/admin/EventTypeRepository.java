package com.example.event_mgm.Repository.admin;

import com.example.event_mgm.model.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;



@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long> {


    boolean existsByName(String name);
}