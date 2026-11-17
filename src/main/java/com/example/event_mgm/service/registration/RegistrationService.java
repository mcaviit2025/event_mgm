package com.example.event_mgm.service.registration;

import com.example.event_mgm.dto.request.SoloRegistrationRequestDto;
import com.example.event_mgm.dto.request.GroupRegistrationRequestDto;
import com.example.event_mgm.dto.response.RegistrationResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface RegistrationService {



    RegistrationResponseDto registerSolo(SoloRegistrationRequestDto request, MultipartFile paymentScreenshot);

    RegistrationResponseDto registerGroup(GroupRegistrationRequestDto request, MultipartFile paymentScreenshot);


}