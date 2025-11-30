package com.vn.gotogether.dto.localguide;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocalGuideApplicationCreateRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String nationalId;

    @NotBlank
    private String frontImageUrl;

    @NotBlank
    private String backImageUrl;

    @NotBlank
    private String localAddress;

    private String description;
}
