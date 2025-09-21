// src/main/java/com/vn/gotogether/service/data/FavoriteService.java
package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.CategoryDto;
import com.vn.gotogether.dto.data.DestinationDto;
import com.vn.gotogether.dto.favorite.FavoritePlaceSummaryResponse;
import com.vn.gotogether.entity.FavoritePlace;
import com.vn.gotogether.entity.Place;
import com.vn.gotogether.entity.PlaceImage;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.repository.data.FavoritePlaceRepository;
import com.vn.gotogether.repository.data.PlaceRepository;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoritePlaceRepository favoriteRepo;
    private final PlaceRepository placeRepo;
    private final UserRepository userRepo;

    public void addFavorite(String placeId) {
        String userId = currentUserIdOrThrow();
        Place place = placeRepo.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found: " + placeId));

        boolean exists = favoriteRepo.existsByUser_IdAndPlace_Id(userId, placeId);
        if (exists) return; // idempotent

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        FavoritePlace fav = FavoritePlace.builder()
                .user(user)
                .place(place)
                .build();
        favoriteRepo.save(fav);
    }

    public void removeFavorite(String placeId) {
        String userId = currentUserIdOrThrow();
        // idempotent
        favoriteRepo.deleteByUser_IdAndPlace_Id(userId, placeId);
    }

    @Transactional(readOnly = true)
    public boolean isMyFavorite(String placeId) {
        String userId = currentUserIdOrThrow();
        return favoriteRepo.existsByUser_IdAndPlace_Id(userId, placeId);
    }

    @Transactional(readOnly = true)
    public long countFavorites(String placeId) {
        return favoriteRepo.countByPlace_Id(placeId);
    }

    @Transactional(readOnly = true)
    public List<FavoritePlaceSummaryResponse> listMyFavorites() {
        String userId = currentUserIdOrThrow();
        List<FavoritePlace> rows = favoriteRepo.findByUserIdFetchPlace(userId);

        return rows.stream().map(fp -> {
            Place p = fp.getPlace();

            String mainImage = (p.getImages() == null) ? null
                    : p.getImages().stream()
                    .findFirst()
                    .map(PlaceImage::getImageUrl)
                    .orElse(null);

            return FavoritePlaceSummaryResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .lat(p.getLat())
                    .lng(p.getLon()) // GIỮ 'lng' cho FE
                    .rating(p.getRating())
                    .address(p.getAddress())
                    .mainImage(mainImage)
                    .category(p.getCategory() == null ? null :
                            CategoryDto.builder()
                                    .id(p.getCategory().getId())
                                    .name(p.getCategory().getName())
                                    .build())
                    .destination(p.getDestination() == null ? null :
                            DestinationDto.builder()
                                    .id(p.getDestination().getId())
                                    .name(p.getDestination().getName())
                                    .build())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listMyFavoriteIds() {
        String userId = currentUserIdOrThrow();
        return favoriteRepo.findPlaceIdsByUserId(userId);
    }

    // ===== Helpers =====
    private String currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new AccessDeniedException("Bạn cần đăng nhập.");
        }

        // Nếu là JWT: ưu tiên lấy claim "id"
        if (auth instanceof JwtAuthenticationToken jat) {
            String idClaim = jat.getToken().getClaimAsString("id");
            if (idClaim != null && !idClaim.isBlank()) {
                // kiểm tra tồn tại
                return userRepo.findById(idClaim)
                        .map(User::getId)
                        .orElseThrow(() -> new AccessDeniedException("Tài khoản không hợp lệ (id)."));
            }
            // nếu không có id claim thì fallback xuống email ở dưới
        }

        // Fallback: dùng email/subject
        String email = auth.getName(); // ví dụ "user@gmail.com"
        return userRepo.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không hợp lệ (email)."));
    }
}
