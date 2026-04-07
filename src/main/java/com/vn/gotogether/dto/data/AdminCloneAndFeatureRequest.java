// src/main/java/com/vn/gotogether/dto/data/AdminCloneAndFeatureRequest.java
package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AdminCloneAndFeatureRequest {
    @NotBlank(message = "ID lịch trình gốc không được để trống")
    private String sourceItineraryId;

    @NotBlank(message = "Overview không được để trống")
    private String overview;

    // Ảnh bìa mới (có thể có hoặc không, nếu bắt buộc thì custom validation)
    private List<MultipartFile> heroImageFiles;
}