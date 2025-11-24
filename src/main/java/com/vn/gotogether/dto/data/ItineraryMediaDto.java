package com.vn.gotogether.dto.data;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryMediaDto {
    private String id;
    private String itineraryId;
    private String userId;
    private String userName;
    private String userAvatar;
    private Integer dayNumber;
    private String mediaType;
    private String mediaUrl;
    private String thumbnailUrl;
    private String caption;
    private Long fileSize;
    private Integer duration;
    private Integer width;
    private Integer height;
    private Integer orderInDay;
    private Instant createdAt;
    private Instant updatedAt;
}