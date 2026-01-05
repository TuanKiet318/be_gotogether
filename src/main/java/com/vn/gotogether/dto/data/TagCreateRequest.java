package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TagCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;
}
