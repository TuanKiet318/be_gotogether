package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BestPlaceDto {
    private String id;
    private String name;
    private Double rating;
    private String mainImage;
    private CategoryDto category;
}