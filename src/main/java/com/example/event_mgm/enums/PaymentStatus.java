package com.example.event_mgm.enums;

public enum PaymentStatus {
    PENDING,    // UTR submitted, awaiting verification
    VERIFIED,   // Payment verified by admin
    REJECTED    // Payment rejected by admin
}