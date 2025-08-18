package com.vn.gotogether.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String token;

    @Column(nullable = false)
    String deviceId;

    @Column(nullable = false)
    Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column
    String userAgent;

    @Column
    String ipAddress;

    @Column
    Instant createdAt;

    @Builder.Default
    @Column(nullable = false)
    boolean revoked = false;

    @PrePersist
    public void beforeCreate() {
        createdAt = Instant.now();
    }
}
