package com.example.event_mgm.controller.public_api;

import com.example.event_mgm.dto.request.SoloRegistrationRequestDto;
import com.example.event_mgm.dto.request.GroupRegistrationRequestDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.RegistrationResponseDto;
import com.example.event_mgm.service.registration.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "Student Registration", description = "Public APIs for student registration")
public class RegistrationController {

    private final RegistrationService registrationService;



    @PostMapping(value = "/solo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register for an event as SOLO participant")
    public ResponseEntity<ApiResponse<RegistrationResponseDto>> registerSolo(
            @Valid @RequestPart("request") SoloRegistrationRequestDto request,
            @RequestPart(value = "paymentScreenshot", required = false) MultipartFile paymentScreenshot) {

        log.info("Solo registration request for event: {}, email: {}",
                request.getEventId(), request.getEmail());

        RegistrationResponseDto response = registrationService.registerSolo(request, paymentScreenshot);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solo registration successful", response));
    }

    // ============================================================
    // GROUP REGISTRATION
    // ============================================================

        @PostMapping(value = "/group", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Register for an event as GROUP")
        public ResponseEntity<ApiResponse<RegistrationResponseDto>> registerGroup(
                @Valid @RequestPart("request") GroupRegistrationRequestDto request,
                @RequestPart(value = "paymentScreenshot", required = false) MultipartFile paymentScreenshot) {
    
            log.info("Group registration request for event: {}, leader: {}",
                    request.getEventId(), request.getLeader().getEmail());
    
            RegistrationResponseDto response = registrationService.registerGroup(request, paymentScreenshot);
    
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Group registration successful", response));
        }

    }