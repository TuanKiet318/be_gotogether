// src/main/java/com/vn/gotogether/dto/favorite/FavoritePlaceSummaryResponse.java
package com.vn.gotogether.dto.favorite;

import com.vn.gotogether.dto.data.CategoryDto;
import com.vn.gotogether.dto.data.DestinationDto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoritePlaceSummaryResponse {
    private String id;
    private String name;
    private Double lat;
    private Double lng;         // giữ 'lng' cho đồng nhất FE
    private Double rating;
    private String address;
    private String mainImage;

    private CategoryDto category;       // tùy chọn hiển thị
    private DestinationDto destination; // tùy chọn hiển thị
}
