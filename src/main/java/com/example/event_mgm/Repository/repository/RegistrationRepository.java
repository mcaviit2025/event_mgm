package com.example.event_mgm.Repository.repository;

import com.example.event_mgm.model.Registration;
import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.event_mgm.enums.RegistrationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {


    boolean existsByEventIdAndEmail(Long eventId, String email);




}