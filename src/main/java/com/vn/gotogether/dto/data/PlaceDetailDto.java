package com.vn.gotogether.dto.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private List<PlaceDto> nearbyPlaces;

    // ---- Thêm cho tính năng yêu thích ----
    private boolean favorited;
    private long favoriteCount;
}
