package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InviteRequestDto {
    @NotBlank
    private String itineraryId;

    @NotBlank @Email
    private String inviteEmail;

    private String role; // EDITOR hoặc VIEWER
}