package com.vn.gotogether.dto.data;

import com.vn.gotogether.entity.BlogMedia;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BlogSummaryResponse {
    private String id;
    private String title;
    private String slug;
    private String excerpt;
    private Integer viewCount;
    private String status;

    private String authorName;
    private String authorAvatar;
    private String content;
    private List<MediaDto> media;

    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;
    private Instant createdAt;
}