package com.vn.gotogether.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContentItem {
    private String type; // paragraph, image
    private String text; // chỉ dùng nếu type = paragraph
    private String src;  // chỉ dùng nếu type = image
    private String alt;  // chỉ dùng nếu type = image
}