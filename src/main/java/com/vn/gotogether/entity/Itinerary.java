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

    /* ---------- Ownership ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* ---------- Basic info ---------- */
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String overview; // mô tả tổng quan (landing page)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /* ---------- Media ---------- */
    @ElementCollection
    @CollectionTable(
            name = "itinerary_hero_images",
            joinColumns = @JoinColumn(name = "itinerary_id")
    )
    @Column(name = "image_url", nullable = false, length = 500)
    @Builder.Default
    private Set<String> imageHero = new LinkedHashSet<>();
    // dùng LinkedHashSet để giữ thứ tự hiển thị

    /* ---------- Visibility & feature ---------- */
    @Column(nullable = false)
    private boolean isFeatured = false;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    /* ---------- Destination ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    /* ---------- Tags (phong cách / chủ đề) ---------- */
    @ManyToMany
    @JoinTable(
            name = "itinerary_tags",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();


    /* ---------- Daily items ---------- */
    @OneToMany(
            mappedBy = "itinerary",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayNumber ASC, orderInDay ASC")
    @Builder.Default
    private List<ItineraryItem> items = new ArrayList<>();

    /* ---------- Extra content ---------- */
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItineraryMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItineraryBlog> blogs = new ArrayList<>();

    /* ---------- Audit ---------- */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

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
