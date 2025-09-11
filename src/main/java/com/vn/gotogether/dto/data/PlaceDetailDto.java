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
public class PlaceDetailDto {
    private String id;
    private String name;
    private Double lat;
    private Double lng;
    private String description;
    private Double rating;
    private String address;
    private String website;
    private String phone;
    private DestinationDto destination;
    private CategoryDto category;
    private List<ImageDto> images;
    private List<ReviewDto> reviews;
}

