package com.vn.gotogether.dto.data;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    private String id;
    private String name;
    private String parentId;
    private String parentName;
}