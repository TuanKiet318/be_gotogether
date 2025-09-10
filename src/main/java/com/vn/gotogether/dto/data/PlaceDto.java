package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceDto {
    private String id;
    private String name;
    private Double lat;
    private Double lng;
    private Double rating;
    private String address;
    private String mainImage;
    private String description;
}