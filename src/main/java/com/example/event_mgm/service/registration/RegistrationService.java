package com.example.event_mgm.service.registration;

import com.example.event_mgm.dto.request.SoloRegistrationRequestDto;
import com.example.event_mgm.dto.request.GroupRegistrationRequestDto;
import com.example.event_mgm.dto.response.RegistrationResponseDto;

public interface RegistrationService {

    // ===== STUDENT ACTIONS =====

    RegistrationResponseDto registerSolo(SoloRegistrationRequestDto request);

    RegistrationResponseDto registerGroup(GroupRegistrationRequestDto request);


}