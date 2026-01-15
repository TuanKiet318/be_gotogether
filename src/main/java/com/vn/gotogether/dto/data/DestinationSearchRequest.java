package com.vn.gotogether.dto.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationSearchRequest {
    private String search;
    private Integer page = 0;
    private Integer size = 9;
    private String sortBy = "name";
    private String sortDirection = "ASC";
}
