package com.vn.gotogether.dto.data;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDto {
    private String id;
    private String name;
    private Double lat;
    private Double lon;
    private String description;
    private Double rating;
    private String address;
    private String website;
    private String phone;

    private DestinationDto destination;
    private CategoryDto category;
    private List<PlaceImageDto> images;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DestinationDto {
        private String id;
        private String name;
        private String country;
        private Double lat;
        private Double lon;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDto {
        private String id;
        private String name;
        private String parentId;
        private String parentName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceImageDto {
        private String id;
        private String imageUrl;
    }
}