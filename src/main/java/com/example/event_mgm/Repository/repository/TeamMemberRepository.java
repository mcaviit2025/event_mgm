package com.example.event_mgm.Repository.repository;

import com.example.event_mgm.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByRegistrationIdAndIsActiveTrueOrderByIdAsc(Long registrationId);

    List<TeamMember> findByRegistrationIdInAndIsActiveTrueOrderByRegistrationIdAscIdAsc(
            List<Long> registrationIds);
}