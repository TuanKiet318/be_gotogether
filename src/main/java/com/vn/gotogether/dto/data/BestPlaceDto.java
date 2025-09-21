package com.vn.gotogether.dto.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestPlaceDto {
    private String id;
    private String name;
    private Double rating;
    private Double lat;

    private Double lng;

    private String description;
    private String mainImage;
    private CategoryDto category;

    // ---- Yêu thích (tuỳ chọn nếu bạn cần) ----
    private boolean favorited;   // user hiện tại đã like chưa
    private long favoriteCount;  // tổng số lượt like
}
