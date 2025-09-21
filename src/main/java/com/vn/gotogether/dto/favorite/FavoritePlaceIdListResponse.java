// src/main/java/com/vn/gotogether/dto/favorite/FavoritePlaceIdListResponse.java
package com.vn.gotogether.dto.favorite;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoritePlaceIdListResponse {
    private List<String> ids;
}
