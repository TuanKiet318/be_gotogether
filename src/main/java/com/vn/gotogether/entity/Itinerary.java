package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "itineraries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Nếu muốn giữ danh sách điểm “yêu thích” nhanh thì để lại,
    // còn chuẩn hóa thì có thể bỏ vì đã có ItineraryItem -> Place.
    @ManyToMany
    @JoinTable(name = "itinerary_attractions",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "attraction_id"))
    @Builder.Default
    private Set<Place> attractions = new HashSet<>();

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC, orderInDay ASC")
    @Builder.Default
    private List<ItineraryItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
