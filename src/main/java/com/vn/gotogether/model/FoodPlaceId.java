package com.vn.gotogether.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPlaceId implements Serializable {
    private String foodId;
    private String placeId;
}

