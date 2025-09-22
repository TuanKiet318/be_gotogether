package com.vn.gotogether.dto.data;

import com.vn.gotogether.entity.ItineraryCollaborator;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollaboratorResponseDto {
    private String userId;
    private String email;
    private String role;
    private Instant addedAt;

    public static CollaboratorResponseDto fromEntity(ItineraryCollaborator collab) {
        return CollaboratorResponseDto.builder()
                .userId(collab.getUser().getId())
                .email(collab.getUser().getEmail())
                .role(collab.getRole().name())
                .addedAt(collab.getAddedAt())
                .build();
    }
}