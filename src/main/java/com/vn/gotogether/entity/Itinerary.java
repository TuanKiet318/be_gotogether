package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable = false)
    private Instant createdAt;

    // Many-to-many qua bảng itinerary_attractions
    @ManyToMany
    @JoinTable(name = "itinerary_attractions",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "attraction_id"))
    @Builder.Default
    private Set<Place> attractions = new HashSet<>();
}
