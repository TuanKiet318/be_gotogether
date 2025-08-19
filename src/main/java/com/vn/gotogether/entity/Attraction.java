package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "attractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name; // "Eo Gió"
    @Column(unique = true, length = 200)
    private String slug;    // "eo-gio"
    private Double lat;
    private Double lng;
    private Double rating; // 0..5

    @ManyToMany
    @JoinTable(
            name = "attraction_tags",
            joinColumns = @JoinColumn(name = "attraction_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @lombok.Builder.Default
    private java.util.Set<Tag> tags = new java.util.HashSet<>();
    @Column(length = 2000)
    private String openingHours;   // "Mon-Sun 06:00-18:30"
    @Column(length = 1000)
    private String coverImageUrl;
    @Column(columnDefinition = "text")
    private String description;
    @Column(nullable = false)
    private Instant createdAt;

    // Optional: ảnh liên quan
    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<AttractionImage> images = new ArrayList<>();
}