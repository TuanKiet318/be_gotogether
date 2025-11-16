package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "blog_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blog_id", "user_id"}))
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;

    @PrePersist
    public void preCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

