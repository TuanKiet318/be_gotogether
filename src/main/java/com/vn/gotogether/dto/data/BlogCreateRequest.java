package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class BlogCreateRequest {
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private String excerpt;

    private String placeId;

    private List<MediaCreateRequest> media;
}