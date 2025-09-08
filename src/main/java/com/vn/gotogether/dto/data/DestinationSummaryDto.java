package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationSummaryDto {
    private String id;
    private String name;
    private String country;
    private String description;
    private Double lat;
    private Double lon;
    private String mainImage;
    private Integer totalPlaces;
    private Integer totalFoods;
}
