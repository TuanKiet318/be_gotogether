package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ImportPlacesResponse {
    int createdCount;
    int skippedDuplicates;
    List<CreatedItem> items;

    @Value
    @Builder
    public static class CreatedItem {
        String id;
        String placeId;
        Integer dayNumber;
        Integer orderInDay;
        String startTime;
        String endTime;
    }
}
