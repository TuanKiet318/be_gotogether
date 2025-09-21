package com.vn.gotogether.dto.data;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class FavoriteCheckResponse {
    private boolean favorited;
}
