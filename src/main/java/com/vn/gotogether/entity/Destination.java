package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true, length = 200)
    private String name; // "Quy Nhơn"
    @Column(unique = true, length = 200)
    private String slug;                  // "quy-nhon"
    private Double lat;
    private Double lng;
    @Column(nullable = false)
    private Instant createdAt;
}
