package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBlogFromItineraryRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String excerpt;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private Boolean includeMedia = true; // Có lấy media từ itinerary không
    private Boolean autoPublish = false;  // Tự động publish hay để draft
}