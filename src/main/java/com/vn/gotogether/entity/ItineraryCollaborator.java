// ItineraryCollaborator.java
package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "itinerary_collaborators",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"itinerary_id", "user_id"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryCollaborator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant addedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    public enum Role { EDITOR, VIEWER }

    @PrePersist
    void prePersist() {
        if (addedAt == null) addedAt = Instant.now();
        if (role == null) role = Role.EDITOR;
    }
}
