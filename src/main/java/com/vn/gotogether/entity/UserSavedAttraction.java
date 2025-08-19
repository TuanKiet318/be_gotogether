package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_saved_attractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSavedAttraction {
    @EmbeddedId
    private UserSavedAttractionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attractionId")
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    @Column(nullable = false)
    private Instant savedAt;
}
