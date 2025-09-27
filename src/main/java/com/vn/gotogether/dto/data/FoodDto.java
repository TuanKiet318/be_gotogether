package com.vn.gotogether.dto.data;

import com.vn.gotogether.model.ContentItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private List<ContentItem> content;
}
