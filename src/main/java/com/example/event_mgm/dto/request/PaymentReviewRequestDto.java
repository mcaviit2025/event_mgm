package com.example.event_mgm.dto.request;

import com.example.event_mgm.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReviewRequestDto {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}
