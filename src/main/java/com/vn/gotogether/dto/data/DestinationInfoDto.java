package com.vn.gotogether.dto.data;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationInfoDto {
    private String id;
    private String infoKey;
    private String infoValue;
    private String imageUrl;
}
