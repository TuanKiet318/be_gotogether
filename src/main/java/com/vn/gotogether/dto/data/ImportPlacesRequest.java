package com.vn.gotogether.dto.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ImportPlacesRequest {

    @Min(1)
    private Integer defaultDay = 1;

    // "APPEND" hoặc "REINDEX"
    private String appendMode = "APPEND";

    // nếu true: bỏ qua place trùng trong cùng day
    private Boolean preventDuplicatesInDay = false;

    @Valid
    private List<ImportItem> items;

    @Data
    public static class ImportItem {
        @NotBlank
        private String placeId;
        private Integer dayNumber;
        private String startTime;
        private String endTime;
        private String description;
        private Double estimatedCost;
        private String transportMode;
        private Integer orderInDay;
    }
}
