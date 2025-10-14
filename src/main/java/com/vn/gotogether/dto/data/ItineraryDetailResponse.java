package com.vn.gotogether.dto.data;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryDetailResponse {
    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Item> items;
    private String destinationId;
    private String destinationName;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String id;
        private String placeId;
        private String placeName;
        private String placeAddress;
        private String placeImage;
        private Double lat;
        private Double lng;
        private Integer dayNumber;
        private Integer orderInDay;
        private LocalTime startTime;
        private LocalTime endTime;
        private String description;
        private Double estimatedCost;
        private String transportMode;
    }
}
