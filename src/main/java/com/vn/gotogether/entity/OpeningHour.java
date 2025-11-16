package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "opening_hours",
        indexes = {@Index(columnList = "place_id"), @Index(columnList = "date")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // optional override for specific date
    @Column(name = "date")
    private LocalDate date;

    // 1 = Monday ... 7 = Sunday (nullable if date != null)
    @Column(name = "weekday")
    private Integer weekday;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
