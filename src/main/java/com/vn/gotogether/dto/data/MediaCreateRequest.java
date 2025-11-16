package com.vn.gotogether.dto.data;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaCreateRequest {

    @NotBlank
    private String url;        // link ảnh/video (đã upload ở đâu đó: S3, Cloudinary...)

    @NotBlank
    private String type;       // "IMAGE" hoặc "VIDEO"

    private String description; // mô tả thêm cho media (optional)
}

