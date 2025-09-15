package com.vn.gotogether.dto.data;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateItineraryItemRequest {

    @NotBlank
    private String placeId;

    @NotNull
    @Min(1)
    private Integer dayNumber;     // Ngày thứ mấy trong chuyến đi (bắt đầu từ 1)

    @Min(1)
    private Integer orderInDay;    // Thứ tự trong ngày (1,2,3...). Có thể để null để server tự gán

    private LocalTime startTime;   // Có thể null nếu không cố định giờ
    private LocalTime endTime;     // Có thể null

    @Size(max = 500)
    private String description;

    @PositiveOrZero
    private Double estimatedCost;

    @Pattern(
            regexp = "WALK|BIKE|CAR|BUS|TRAIN|FLIGHT|BOAT",
            message = "transportMode must be one of WALK, BIKE, CAR, BUS, TRAIN, FLIGHT, BOAT"
    )
    private String transportMode;
}
