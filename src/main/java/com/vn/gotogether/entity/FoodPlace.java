package com.vn.gotogether.entity;

import com.vn.gotogether.model.FoodPlaceId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_place")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FoodPlaceId.class) // chỉ định class làm composite key
public class FoodPlace {

    @Id
    @Column(name = "food_id")
    private String foodId;

    @Id
    @Column(name = "place_id")
    private String placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", insertable = false, updatable = false)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    private Place place;

    @Column(name = "note")
    private String note;
}

