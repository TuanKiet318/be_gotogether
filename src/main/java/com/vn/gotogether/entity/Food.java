package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", referencedColumnName = "id")
    private Destination destination;

    // Constructor cho Builder và business logic (không bao gồm id vì auto-generated)
    public Food(String name, String description, String imageUrl, Destination destination) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.destination = destination;
    }

    // Constructor với tất cả fields (để Lombok Builder có thể sử dụng)
    @Builder
    public Food(String id, String name, String description, String imageUrl, Destination destination) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Food{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", destination=" + (destination != null ? destination.getId() : null) +
                '}';
    }
}