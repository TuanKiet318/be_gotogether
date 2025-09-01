package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationResponseDto {
    private String id;
    private String name;
    private String country;
    private String description;
    private Double lat;
    private Double lon;
    private Integer totalPlaces;
}