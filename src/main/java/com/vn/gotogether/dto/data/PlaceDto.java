package com.vn.gotogether.dto.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
}
