package com.vn.gotogether.dto.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.gotogether.entity.Place;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // ---- Thêm cho tính năng yêu thích ----
    private boolean favorited;     // user hiện tại đã like chưa
    private long favoriteCount;    // tổng số lượt like/yeu thích

    public PlaceDto(Place p) {
        this.id = p.getId();
        this.name = p.getName();
        this.lat = p.getLat();
        this.lng = p.getLon();
        this.rating = p.getRating();
        this.address = p.getAddress();
        this.mainImage = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().iterator().next().getImageUrl()
                : null;
        this.description = p.getDescription();
        this.favorited = false;
        this.favoriteCount = 0;
    }

}
