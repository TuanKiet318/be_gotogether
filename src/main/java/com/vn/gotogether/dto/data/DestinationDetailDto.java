package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationDetailDto {
    private String id;
    private String name;
    private String country;
    private String description;
    private Double lat;
    private Double lng;
    private List<ImageDto> images;
    private List<BestPlaceDto> bestPlaces;
}
