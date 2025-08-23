package com.vn.gotogether.entity;

import com.vn.gotogether.entity.Place;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;
}
