package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BlogDetailResponse {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private Integer viewCount;
    private String status;
    private Boolean isPublic;

    // Author info
    private String authorId;
    private String authorName;
    private String authorAvatar;

    // Media
    private List<MediaDto> media;

    // Stats
    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;

    // Itinerary link (nếu có)
    private String itineraryId;
    private String itineraryTitle;

    // Comments
    private List<CommentDto> comments;

    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class CommentDto {
        private String id;
        private String userId;
        private String userName;
        private String userAvatar;
        private String comment;
        private Instant createdAt;
    }
}