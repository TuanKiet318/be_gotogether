package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true, length = 50)
    private String code; // "beach", "viewpoint"
    @Column(nullable = false, length = 150)
    private String name;
    @Column(unique = true, length = 150)
    private String slug;
    @Column(columnDefinition = "text")
    private String description;
    private Integer sortOrder;
    @Column(nullable = false)
    private Instant createdAt;
}
