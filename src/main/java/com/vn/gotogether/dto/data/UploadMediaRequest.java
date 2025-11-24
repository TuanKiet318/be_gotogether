package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadMediaRequest {
    @NotBlank(message = "Media URL không được để trống")
    private String mediaUrl;

    private String thumbnailUrl;

    @NotNull(message = "Media type không được để trống")
    private String mediaType; // IMAGE hoặc VIDEO

    private Integer dayNumber;
    private String caption;
    private Long fileSize;
    private Integer duration;
    private Integer width;
    private Integer height;
}