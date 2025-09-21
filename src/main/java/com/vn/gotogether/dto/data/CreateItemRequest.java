package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateItemRequest {
    @NotBlank
    private String placeId;

    // optional: nếu null sẽ mặc định = 1
    private Integer dayNumber;

    // optional: nếu null sẽ auto = max(orderInDay)+1 trong ngày
    private Integer orderInDay;

    // "HH:mm" hoặc null
    private String startTime;
    private String endTime;

    @Size(max = 500)
    private String description;

    private Double estimatedCost;
    // WALK/BIKE/CAR/BUS/TRAIN/FLIGHT/BOAT
    private String transportMode;
}
