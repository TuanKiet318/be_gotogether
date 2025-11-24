package com.vn.gotogether.dto.data;

import lombok.Data;

@Data
public class UpdateMediaRequest {
    private String caption;
    private Integer orderInDay;
    private Integer dayNumber;
}