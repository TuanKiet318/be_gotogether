package com.vn.gotogether.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // "sunset"
    @Column(nullable = false, length = 100)
    private String name; // "Ngắm hoàng hôn"
    @Column(unique = true, length = 100)
    private String slug; // "sunset"
    @Column(columnDefinition = "text")
    private String description;
    @Column(nullable = false)
    private Instant createdAt;
}
