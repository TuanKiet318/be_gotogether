package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ItineraryItemDto {
    String id;
    String placeId;
    String placeName;
    Integer dayNumber;
    Integer orderInDay;
    String startTime;   // "HH:mm" hoặc null
    String endTime;
    String description;
    Double estimatedCost;
    String transportMode; // enum name hoặc null
}
