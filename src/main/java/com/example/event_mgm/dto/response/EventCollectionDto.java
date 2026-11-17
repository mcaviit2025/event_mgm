package com.example.event_mgm.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EventCollectionDto {

    private Long eventId;
    private String eventName;
    private BigDecimal eventFee;
    private long registrationCount;
    private long verifiedPaymentCount;
    private long pendingPaymentCount;
    private BigDecimal amountCollected;
}
