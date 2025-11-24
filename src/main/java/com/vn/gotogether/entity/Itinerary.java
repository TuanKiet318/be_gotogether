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

        @Column(nullable = false)
        private boolean isFeatured = false;

        @Column(name = "is_public", nullable = false)
        private boolean isPublic = false;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "destination_id", nullable = false)
        private Destination destination;


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

        @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ItineraryMedia> media = new ArrayList<>();

        @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ItineraryBlog> blogs = new ArrayList<>();
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
