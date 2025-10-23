package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PlacesByCategoryResponseDto {
    private DestinationDto destination;
    private List<CategoryPlacesDto> categoryPlaces;
    private Integer totalCategories;
    private Integer totalPlaces;
}

