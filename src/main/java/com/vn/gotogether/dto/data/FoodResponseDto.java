package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponseDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String destinationId;
    private String destinationName;
}