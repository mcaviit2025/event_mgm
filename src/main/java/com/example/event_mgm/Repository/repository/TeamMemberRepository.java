package com.example.event_mgm.Repository.repository;

import com.example.event_mgm.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    }