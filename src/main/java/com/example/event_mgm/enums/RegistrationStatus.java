package com.example.event_mgm.enums;

public enum RegistrationStatus {
    PENDING,    // Submitted, awaiting admin action
    APPROVED,   // Approved by admin
    REJECTED,   // Rejected by admin
    CANCELLED   // Cancelled (by student or admin)
}