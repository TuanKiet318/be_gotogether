package com.vn.gotogether.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "itinerary_items",
        indexes = {
                @Index(name = "idx_item_itin_day", columnList = "itinerary_id, dayNumber"),
                @Index(name = "idx_item_place", columnList = "place_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItineraryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // Ngày thứ mấy trong chuyến đi (bắt đầu từ 1)
    @Column(nullable = false)
    private Integer dayNumber;

    // Thứ tự hiển thị trong ngày (1,2,3…)
    @Column(nullable = false)
    private Integer orderInDay;

    // Thời gian trong ngày (nếu không cố định giờ có thể để null)
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(length = 500)
    private String description;

    // Tuỳ chọn: chi phí ước tính, phương tiện, v.v.
    private Double estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransportMode transportMode;

    public enum TransportMode { WALK, BIKE, CAR, BUS, TRAIN, FLIGHT, BOAT }
}
