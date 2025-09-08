package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriesResponseDto {
    private DestinationDto destination;
    private List<CategoryWithCountDto> categories;
}