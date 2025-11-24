package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaStatsDto {
    private long totalMedia;
    private long totalImages;
    private long totalVideos;
    private int daysWithMedia;
}