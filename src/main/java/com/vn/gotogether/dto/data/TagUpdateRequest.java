package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TagUpdateRequest {

    @NotBlank
    private String name;

    private String description;
}
