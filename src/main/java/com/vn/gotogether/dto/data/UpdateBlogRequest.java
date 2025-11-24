package com.vn.gotogether.dto.data;

import lombok.Data;

@Data
public class UpdateBlogRequest {
    private String title;
    private String content;
    private String excerpt;
    private Boolean isPublic;
}