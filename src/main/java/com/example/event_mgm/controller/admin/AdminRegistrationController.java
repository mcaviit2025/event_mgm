//package com.example.event_mgm.controller.admin;
//
//import com.example.event_mgm.dto.request.PaymentVerificationDto;
//import com.example.event_mgm.dto.request.RejectionDto;
//import com.example.event_mgm.dto.response.AdminRegistrationViewDto;
//import com.example.event_mgm.dto.response.ApiResponse;
//import com.example.event_mgm.dto.response.RegistrationResponseDto;
//import com.example.event_mgm.enums.PaymentStatus;
//import com.example.event_mgm.enums.RegistrationStatus;
//import com.example.event_mgm.security.CustomUserDetails;
//import com.example.event_mgm.service.registration.RegistrationService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequestMapping("/v1/admin/registrations")
//@RequiredArgsConstructor
//@Tag(name = "Admin Registration Management", description = "APIs for managing registrations (Admin only)")
//@SecurityRequirement(name = "Bearer Authentication")
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminRegistrationController {
//
//    private final RegistrationService registrationService;
//
//    // ============================================================
//    // GET ALL REGISTRATIONS (With Filters)
//    // ============================================================
//
//    @GetMapping
//    @Operation(summary = "Get all registrations with filters")
//    public ResponseEntity<ApiResponse<Page<AdminRegistrationViewDto>>> getAllRegistrations(
//            @PageableDefault(size = 20, sort = "registeredAt", direction = Sort.Direction.DESC) Pageable pageable,
//            @RequestParam(required = false) Long eventId,
//            @RequestParam(required = false) RegistrationStatus status,
//            @RequestParam(required = false) PaymentStatus paymentStatus) {
//
//        log.info("Fetching registrations with filters - eventId: {}, status: {}, paymentStatus: {}",
//                eventId, status, paymentStatus);
//
//        Page<AdminRegistrationViewDto> response = registrationService.getAllRegistrations(
//                pageable, eventId, status, paymentStatus);
//
//        return ResponseEntity.ok(ApiResponse.success("Registrations retrieved successfully", response));
//    }
//
//    // ============================================================
//    // GET REGISTRATION BY ID (Admin View)
//    // ============================================================
//
//    @GetMapping("/{id}")
//    @Operation(summary = "Get registration details for admin")
//    public ResponseEntity<ApiResponse<AdminRegistrationViewDto>> getRegistrationById(
//            @PathVariable Long id) {
//
//        log.info("Fetching registration details for admin - ID: {}", id);
//
//        AdminRegistrationViewDto response = registrationService.getRegistrationForAdmin(id);
//
//        return ResponseEntity.ok(ApiResponse.success("Registration details retrieved successfully", response));
//    }
//
//    // ============================================================
//    // GET REGISTRATIONS BY EVENT
//    // ============================================================
//
//    @GetMapping("/events/{eventId}")
//    @Operation(summary = "Get all registrations for a specific event")
//    public ResponseEntity<ApiResponse<Page<AdminRegistrationViewDto>>> getRegistrationsByEvent(
//            @PathVariable Long eventId,
//            @PageableDefault(size = 20, sort = "registeredAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        log.info("Fetching registrations for event ID: {}", eventId);
//
//        Page<AdminRegistrationViewDto> response = registrationService.getRegistrationsByEvent(eventId, pageable);
//
//        return ResponseEntity.ok(ApiResponse.success("Event registrations retrieved successfully", response));
//    }
//
//    // ============================================================
//    // VERIFY PAYMENT
//    // ============================================================
//
//    @PatchMapping("/{id}/verify-payment")
//    @Operation(summary = "Verify payment for a registration")
//    public ResponseEntity<ApiResponse<RegistrationResponseDto>> verifyPayment(
//            @PathVariable Long id,
//            @Valid @RequestBody PaymentVerificationDto verificationDto) {
//
//        log.info("Verifying payment for registration ID: {} - Status: {}", id, verificationDto.getPaymentStatus());
//
//        Long adminId = getCurrentAdminId();
//        RegistrationResponseDto response = registrationService.verifyPayment(
//                id, verificationDto.getPaymentStatus(), adminId);
//
//        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
//    }
//
//    // ============================================================
//    // APPROVE REGISTRATION
//    // ============================================================
//
//    @PatchMapping("/{id}/approve")
//    @Operation(summary = "Approve a registration")
//    public ResponseEntity<ApiResponse<RegistrationResponseDto>> approveRegistration(
//            @PathVariable Long id) {
//
//        log.info("Approving registration ID: {}", id);
//
//        Long adminId = getCurrentAdminId();
//        RegistrationResponseDto response = registrationService.approveRegistration(id, adminId);
//
//        return ResponseEntity.ok(ApiResponse.success("Registration approved successfully", response));
//    }
//
//    // ============================================================
//    // REJECT REGISTRATION
//    // ============================================================
//
//    @PatchMapping("/{id}/reject")
//    @Operation(summary = "Reject a registration")
//    public ResponseEntity<ApiResponse<RegistrationResponseDto>> rejectRegistration(
//            @PathVariable Long id,
//            @Valid @RequestBody RejectionDto rejectionDto) {
//
//        log.info("Rejecting registration ID: {} - Reason: {}", id, rejectionDto.getReason());
//
//        Long adminId = getCurrentAdminId();
//        RegistrationResponseDto response = registrationService.rejectRegistration(
//                id, rejectionDto.getReason(), adminId);
//
//        return ResponseEntity.ok(ApiResponse.success("Registration rejected successfully", response));
//    }
//
//    // ============================================================
//    // HELPER METHODS
//    // ============================================================
//
//    private Long getCurrentAdminId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        return userDetails.getId();
//    }
//}