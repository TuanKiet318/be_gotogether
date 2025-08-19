package com.vn.gotogether.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Entity
@Table(name = "attraction_images"
        )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @Column(nullable = false, length = 1000)
    private String url;
    @Column(length = 200)
    private String title;
    private Boolean isCover = false;
    private Integer position = 0;
}
