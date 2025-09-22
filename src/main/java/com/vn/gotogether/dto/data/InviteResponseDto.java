package com.vn.gotogether.dto.data;

import com.vn.gotogether.entity.ItineraryInvite;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteResponseDto {
    private String id;
    private String itineraryId;
    private String inviteEmail;
    private String inviterEmail;
    private String status;
    private String role;
    private Instant createdAt;
    private Instant expiresAt;

    public static InviteResponseDto fromEntity(ItineraryInvite invite) {
        return InviteResponseDto.builder()
                .id(invite.getId())
                .itineraryId(
                        invite.getItinerary() != null ? invite.getItinerary().getId() : null
                )
                .inviteEmail(invite.getInviteEmail())
                .inviterEmail(
                        invite.getInviter() != null ? invite.getInviter().getEmail() : null
                )
                .status(invite.getStatus() != null ? invite.getStatus().name() : null)
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .role(invite.getRole() != null ? invite.getRole().name() : null)
                .build();
    }
}
