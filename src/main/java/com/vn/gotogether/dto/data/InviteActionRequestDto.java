package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InviteActionRequestDto {
    @NotBlank
    private String token;

    @NotBlank
    private String status; // ACCEPTED hoặc DECLINED
}