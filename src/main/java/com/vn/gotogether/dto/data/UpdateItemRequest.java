package com.vn.gotogether.dto.data;

import lombok.Data;

@Data
public class UpdateItemRequest {
    private Integer dayNumber;     // move sang ngày khác
    private Integer orderInDay;    // đổi vị trí trong ngày
    private String startTime;      // "HH:mm"
    private String endTime;
    private String description;
    private Double estimatedCost;
    private String transportMode;  // enum name
}
