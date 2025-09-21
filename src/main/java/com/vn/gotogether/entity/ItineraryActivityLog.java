package com.vn.gotogether.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "itinerary_activity_logs",
        indexes = {
                @Index(name = "idx_log_itinerary_time", columnList = "itinerary_id, created_at"),
                @Index(name = "idx_log_actor", columnList = "actor_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor; // có thể null nếu hệ thống ghi

    @Column(name = "action", length = 64, nullable = false)
    private String action; // ví dụ: IMPORT_PLACES, ITIN_ITEM_CREATED,...

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @PrePersist void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
