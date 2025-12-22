package com.vn.gotogether.dto.data;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryFeaturedDetailResponse {

    /* ---------- Core ---------- */
    private String id;
    private String title;

    private LocalDate startDate;
    private LocalDate endDate;

    /* ---------- Destination ---------- */
    private String destinationId;
    private String destinationName;

    /* ---------- Featured Content ---------- */
    private String overview;               // mô tả tổng quan
    private Set<String> heroImages;         // ảnh banner / cover
    private Set<TagResponse> tags;          // "Ẩm thực", "Phiêu lưu", ...

    /* ---------- Timeline ---------- */
    private List<Item> items;

    /* ===================================================== */

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {

        private String id;

        private String placeId;
        private String placeName;
        private String placeAddress;
        private String placeImage;

        private Double lat;
        private Double lng;

        private Integer dayNumber;
        private Integer orderInDay;

        private LocalTime startTime;
        private LocalTime endTime;

        private String description;     // note của item
        private Double estimatedCost;
        private String transportMode;
    }

    /* ===================================================== */

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagResponse {
        private String id;
        private String name;   // "Ẩm thực", "Phiêu lưu"
        private String slug;   // "am-thuc", "phieu-luu"
    }
}
