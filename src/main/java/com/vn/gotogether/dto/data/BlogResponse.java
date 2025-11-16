package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BlogResponse {
    private String id;
    private String content;
    private String userName;
    private String userAvatar;
    private LocalDateTime createdAt;
    private List<MediaDto> media;
    private long likes;
    private long comments;
}

