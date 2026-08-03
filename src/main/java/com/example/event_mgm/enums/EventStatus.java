package com.example.event_mgm.enums;

public enum EventStatus {
    DRAFT,      // Admin is creating/editing
    OPEN,       // Accepting registrations
    CLOSED,     // Registrations closed (event upcoming)
    COMPLETED,  // Event concluded
    CANCELLED   // Event cancelled
}