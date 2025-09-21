package com.vn.gotogether.dto.data;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoritePlaceSummary {
    private String placeId;
    private String name;
    private String address;
    private Double rating;
    private Double lat;
    private Double lon;
    private String imageUrl;
    private LocalDateTime favoritedAt;
}
