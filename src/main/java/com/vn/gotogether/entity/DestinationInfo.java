package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "destination_infos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationInfo {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(name = "info_key", nullable = false, length = 100)
    private String infoKey;

    @Column(name = "info_value", columnDefinition = "TEXT")
    private String infoValue;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}
