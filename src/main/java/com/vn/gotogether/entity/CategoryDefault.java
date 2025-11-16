package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "category_defaults")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDefault {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, unique = true)
    private Category category;

    private LocalTime defaultOpen;
    private LocalTime defaultClose;

    // default visit length in minutes
    private Integer defaultVisitMinutes;
}
