package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryPlacesDto {
    private CategoryDto category;
    private PlaceDto nearestPlace;
    private Double distance; // khoảng cách tính bằng km (optional)
}