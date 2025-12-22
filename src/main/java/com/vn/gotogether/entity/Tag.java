package com.vn.gotogether.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Entity
@Table(name = "tags")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // key nội bộ: foodie, hiking, adventure
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // hiển thị cho user: Ẩm thực, Leo núi & Trekking
    @Column(nullable = false, length = 100)
    private String name;

    // mô tả ngắn cho admin / tooltip
    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
