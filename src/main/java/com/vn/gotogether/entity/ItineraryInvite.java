package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "itinerary_invites", indexes = {
        @Index(columnList = "invite_token"),
        @Index(columnList = "invite_email")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    // inviter (owner or collaborator who invites)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id")
    private User inviter;

    // email of the invitee (maybe not yet registered)
    @Column(name = "invite_email", nullable = false, length = 255)
    private String inviteEmail;

    @Column(name = "invite_token", nullable = false, unique = true, length = 64)
    private String inviteToken;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    public enum Status { PENDING, ACCEPTED, DECLINED, EXPIRED }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.PENDING;
    }
}
