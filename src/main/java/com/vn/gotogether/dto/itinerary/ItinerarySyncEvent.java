package com.vn.gotogether.dto.itinerary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItinerarySyncEvent {
    private String action;      // CREATE, UPDATE, DELETE
    private String itineraryId;
    private String triggerBy;   // userId của người vừa thực hiện (để frontend của người đó có thể bỏ qua event này tránh trùng lặp)
    private Object data;        // Chứa ItineraryItemDto (nếu CREATE/UPDATE) hoặc itemId (nếu DELETE)
}