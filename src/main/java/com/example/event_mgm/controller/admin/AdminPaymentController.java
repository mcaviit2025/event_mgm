package com.example.event_mgm.controller.admin;

import com.example.event_mgm.dto.request.PaymentReviewRequestDto;
import com.example.event_mgm.dto.response.AdminPaymentReviewDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.CollectionsResponseDto;
import com.example.event_mgm.service.registration.PaymentManagementService;
import com.example.event_mgm.service.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentManagementService paymentManagementService;
    private final FileStorageService fileStorageService;

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<AdminPaymentReviewDto>>> getPayments() {
        return ResponseEntity.ok(ApiResponse.success(
                "Registration payments retrieved successfully",
                paymentManagementService.getPayments()));
    }

    @PatchMapping("/payments/{registrationId}")
    public ResponseEntity<ApiResponse<AdminPaymentReviewDto>> reviewPayment(
            @PathVariable Long registrationId,
            @Valid @RequestBody PaymentReviewRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment status updated successfully",
                paymentManagementService.reviewPayment(registrationId, request)));
    }

    @GetMapping("/payments/{registrationId}/proof")
    public ResponseEntity<byte[]> getPaymentProof(@PathVariable Long registrationId) {
        String proofPath = paymentManagementService.getPaymentProofPath(registrationId);
        String filename = Paths.get(proofPath).getFileName().toString();
        return ResponseEntity.ok()
                .contentType(fileStorageService.getScreenshotMediaType(proofPath))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).build().toString())
                .body(fileStorageService.readPaymentScreenshot(proofPath));
    }

    @GetMapping("/collections")
    public ResponseEntity<ApiResponse<CollectionsResponseDto>> getCollections() {
        return ResponseEntity.ok(ApiResponse.success(
                "Collections retrieved successfully",
                paymentManagementService.getCollections()));
    }
}
