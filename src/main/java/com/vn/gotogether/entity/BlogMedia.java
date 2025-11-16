package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blog_media")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlogMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE / VIDEO

    private String description;

    public enum MediaType {
        IMAGE, VIDEO
    }
}

