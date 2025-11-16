package com.vn.gotogether.dto.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MediaDto {
    private String url;
    private String type;
    private String description;
}
