package com.example.event_mgm.service.registration;

import com.example.event_mgm.dto.SoloRegisterRequestDto;
import com.example.event_mgm.dto.SoloRegistrationResponseDto;

public interface SoloRegistrationService {

    SoloRegistrationResponseDto soloRegistration(SoloRegisterRequestDto requestDto);

}
