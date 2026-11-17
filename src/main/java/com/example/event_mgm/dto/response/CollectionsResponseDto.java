package com.example.event_mgm.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CollectionsResponseDto {

    private long registrationCount;
    private long verifiedPaymentCount;
    private BigDecimal amountCollected;
    private List<EventCollectionDto> events;
}
