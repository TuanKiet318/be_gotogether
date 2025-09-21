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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private String id;
        private String placeId;
        private Integer dayNumber;
        private Integer orderInDay;
        private LocalTime startTime;
        private LocalTime endTime;
        private String description;
        private Double estimatedCost;
        private String transportMode; // trả về dạng String cho client
    }
}
