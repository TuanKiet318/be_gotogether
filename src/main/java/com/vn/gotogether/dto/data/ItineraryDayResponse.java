package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDayResponse {
    private String itineraryId;
    private String message;
    private LocalDate newStartDate;
    private LocalDate newEndDate;
    private int totalDays;
    private int itemsAffected;
    private int itemsDeleted;
}