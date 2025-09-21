// src/main/java/com/vn/gotogether/controller/data/FavoriteController.java
package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.favorite.FavoritePlaceCheckResponse;
import com.vn.gotogether.dto.favorite.FavoritePlaceCountResponse;
import com.vn.gotogether.dto.favorite.FavoritePlaceIdListResponse;
import com.vn.gotogether.dto.favorite.FavoritePlaceSummaryResponse;
import com.vn.gotogether.service.data.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites/places")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{placeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoritePlaceCheckResponse addFavorite(@PathVariable String placeId) {
        favoriteService.addFavorite(placeId);
        return FavoritePlaceCheckResponse.builder().favorited(true).build();
    }

    @DeleteMapping("/{placeId}")
    public FavoritePlaceCheckResponse removeFavorite(@PathVariable String placeId) {
        favoriteService.removeFavorite(placeId);
        return FavoritePlaceCheckResponse.builder().favorited(false).build();
    }

    @GetMapping("/mine")
    public List<FavoritePlaceSummaryResponse> listMyFavorites() {
        return favoriteService.listMyFavorites();
    }

    @GetMapping("/mine/ids")
    public FavoritePlaceIdListResponse listMyFavoriteIds() {
        return FavoritePlaceIdListResponse.builder()
                .ids(favoriteService.listMyFavoriteIds())
                .build();
    }

        @GetMapping("/check")
        public FavoritePlaceCheckResponse check(@RequestParam String placeId) {
            boolean favorited = favoriteService.isMyFavorite(placeId);
            return FavoritePlaceCheckResponse.builder().favorited(favorited).build();
        }

        @GetMapping("/count/{placeId}")
        public FavoritePlaceCountResponse count(@PathVariable String placeId) {
            long count = favoriteService.countFavorites(placeId);
            return FavoritePlaceCountResponse.builder().count(count).build();
        }
    }
