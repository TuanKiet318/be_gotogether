// src/main/java/com/vn/gotogether/entity/FavoritePlace.java
package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "favorite_place",
        uniqueConstraints = @UniqueConstraint(name = "uk_fav_user_place", columnNames = {"user_id","place_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoritePlace {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fav_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_fav_place"))
    private Place place;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
