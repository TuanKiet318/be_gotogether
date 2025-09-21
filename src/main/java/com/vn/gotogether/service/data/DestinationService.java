package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.repository.data.*;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceRepository placeRepository;
    private final FoodRepository foodRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;


    public DestinationService(DestinationRepository destinationRepository,
                              CategoryRepository categoryRepository,
                              PlaceRepository placeRepository,
                              FoodRepository foodRepository,
                              FavoritePlaceRepository favoritePlaceRepository,
                              UserRepository userRepository) {
        this.destinationRepository = destinationRepository;
        this.categoryRepository = categoryRepository;
        this.placeRepository = placeRepository;
        this.foodRepository = foodRepository;
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.userRepository = userRepository;
    }

    public List<DestinationSummaryDto> getAllDestinations() {
        List<Destination> destinations = destinationRepository.findAllWithImages();
        return destinations.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    public DestinationDetailDto getDestinationDetail(String destinationId) {
        Destination destination = destinationRepository.findByIdWithDetails(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));

        return convertToDetailDto(destination);
    }

    public CategoriesResponseDto getCategoriesByDestination(String destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));

        List<Object[]> results = categoryRepository.findCategoriesWithPlaceCountByDestination(destinationId);
        List<CategoryWithCountDto> categories = results.stream()
                .map(result -> CategoryWithCountDto.builder()
                        .id(((Category) result[0]).getId())
                        .name(((Category) result[0]).getName())
                        .placeCount(((Long) result[1]).intValue())
                        .build())
                .collect(Collectors.toList());

        return CategoriesResponseDto.builder()
                .destination(DestinationDto.builder()
                        .id(destination.getId())
                        .name(destination.getName())
                        .build())
                .categories(categories)
                .build();
    }
    public PlacesResponseDto getPlacesByCategory(String destinationId, String categoryId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        List<Place> places = placeRepository.findByDestinationAndCategory(destinationId, categoryId);
        List<String> placeIds = places.stream().map(Place::getId).collect(Collectors.toList());

        // Lấy user hiện tại (có thể null nếu chưa login)
        String userId = currentUserIdOrNull();

        // Batch maps (final để dùng trong lambda)
        final Map<String, Boolean> favMap = buildFavoritedMap(userId, placeIds);
        final Map<String, Long> favCountMap = buildFavoriteCountMap(placeIds);

        List<PlaceDto> placeDtos = places.stream()
                .map(p -> {
                    String mainImage = (p.getImages() == null)
                            ? null
                            : p.getImages().stream()
                            .findFirst()
                            .map(PlaceImage::getImageUrl)
                            .orElse(null);

                    return PlaceDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .lat(p.getLat())
                            .lng(p.getLon()) // giữ nguyên lng
                            .rating(p.getRating())
                            .address(p.getAddress())
                            .mainImage(mainImage)
                            .description(p.getDescription())
                            .favorited(favMap.getOrDefault(p.getId(), false))
                            .favoriteCount(favCountMap.getOrDefault(p.getId(), 0L))
                            .build();
                })
                .collect(Collectors.toList());


        return PlacesResponseDto.builder()
                .category(CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .destination(DestinationDto.builder()
                        .id(destination.getId())
                        .name(destination.getName())
                        .build())
                .places(placeDtos)
                .total(placeDtos.size())
                .build();
    }
//    public PlacesResponseDto getPlacesByCategory(String destinationId, String categoryId) {
//        Destination destination = destinationRepository.findById(destinationId)
//                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));
//
//        Category category = categoryRepository.findById(categoryId)
//                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//        List<Place> places = placeRepository.findByDestinationAndCategory(destinationId, categoryId);
//        List<String> placeIds = places.stream().map(Place::getId).toList();
//
//        // Lấy user hiện tại (có thể null nếu chưa đăng nhập)
//        String userId = currentUserIdOrNull();
//
//        // Batch: map placeId -> favorited / favoriteCount (dùng biến final để dùng trong lambda)
//        final Map<String, Boolean> favMap = buildFavoritedMap(userId, placeIds);
//        final Map<String, Long> favCountMap = buildFavoriteCountMap(placeIds);
//
//        List<PlaceDto> placeDtos = places.stream()
//                .map(p -> {
//                    String mainImage = (p.getImages() != null && !p.getImages().isEmpty())
//                            ? p.getImages().get(0).getImageUrl()
//                            : null;
//                    return PlaceDto.builder()
//                            .id(p.getId())
//                            .name(p.getName())
//                            .lat(p.getLat())
//                            .lng(p.getLon()) // giữ nguyên lng
//                            .rating(p.getRating())
//                            .address(p.getAddress())
//                            .mainImage(mainImage)
//                            .description(p.getDescription())
//                            .favorited(favMap.getOrDefault(p.getId(), false))
//                            .favoriteCount(favCountMap.getOrDefault(p.getId(), 0L))
//                            .build();
//                })
//                .toList();
//
//        return PlacesResponseDto.builder()
//                .category(CategoryDto.builder()
//                        .id(category.getId())
//                        .name(category.getName())
//                        .build())
//                .destination(DestinationDto.builder()
//                        .id(destination.getId())
//                        .name(destination.getName())
//                        .build())
//                .places(placeDtos)
//                .total(placeDtos.size())
//                .build();
//    }

    public FoodsResponseDto getFoodsByDestination(String destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));

        List<Food> foods = foodRepository.findByDestinationIdOrderByName(destinationId);
        List<FoodDto> foodDtos = foods.stream()
                .map(this::convertToFoodDto)
                .collect(Collectors.toList());

        return FoodsResponseDto.builder()
                .destination(DestinationDto.builder()
                        .id(destination.getId())
                        .name(destination.getName())
                        .build())
                .foods(foodDtos)
                .total(foodDtos.size())
                .build();
    }

    public PlaceDetailDto getPlaceDetail(String placeId) {
        Place place = placeRepository.findByIdWithDetails(placeId)
                .orElseThrow(() -> new EntityNotFoundException("Place not found with id: " + placeId));

        List<Place> nearbyPlaces = placeRepository.findNearbyPlaces(
                place.getLat(),
                place.getLon(),
                place.getDestination().getId(),
                place.getId(),
                PageRequest.of(0, 6)
        );

        // ===== Batch favorite: place chính + nearby
        String userId = currentUserIdOrNull();

        List<String> allIds = new java.util.ArrayList<>(nearbyPlaces.size() + 1);
        allIds.add(place.getId());
        allIds.addAll(nearbyPlaces.stream().map(Place::getId).toList());

        final java.util.Map<String, Boolean> favMap = buildFavoritedMap(userId, allIds);
        final java.util.Map<String, Long> favCountMap = buildFavoriteCountMap(allIds);

        // map nearby kèm favorited/favoriteCount
        List<PlaceDto> nearbyDtos = nearbyPlaces.stream()
                .map(p -> convertToPlaceDto(
                        p,
                        favMap.getOrDefault(p.getId(), false),
                        favCountMap.getOrDefault(p.getId(), 0L)
                ))
                .toList();

        // map place chính kèm favorited/favoriteCount
        PlaceDetailDto dto = convertToPlaceDetailDto(
                place,
                favMap.getOrDefault(place.getId(), false),
                favCountMap.getOrDefault(place.getId(), 0L)
        );
        dto.setNearbyPlaces(nearbyDtos);
        return dto;
    }



    // Helper methods
    private DestinationSummaryDto convertToSummaryDto(Destination destination) {
        String mainImage = destination.getImages().stream()
                .findFirst()
                .map(DestinationImage::getImageUrl)
                .orElse(null);

        Integer totalPlaces = destinationRepository.countPlacesByDestinationId(destination.getId());
        Integer totalFoods = destinationRepository.countFoodsByDestinationId(destination.getId());

        return DestinationSummaryDto.builder()
                .id(destination.getId())
                .name(destination.getName())
                .country(destination.getCountry())
                .description(destination.getDescription())
                .lat(destination.getLat())
                .lon(destination.getLon())
                .mainImage(mainImage)
                .totalPlaces(totalPlaces)
                .totalFoods(totalFoods)
                .build();
    }

    private DestinationDetailDto convertToDetailDto(Destination destination) {
        List<ImageDto> images = destination.getImages().stream()
                .map(img -> ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        // Get top attractions (non-restaurant places)
        List<Place> topAttractions = placeRepository.findTopAttractionsByDestination(
                destination.getId(), PageRequest.of(0, 4));

        // Get top restaurants and food places
        List<Place> topRestaurants = placeRepository.findTopRestaurantsByDestination(
                destination.getId(), PageRequest.of(0, 4));

        List<BestPlaceDto> bestPlaces = topAttractions.stream()
                .map(this::convertToBestPlaceDto)
                .collect(Collectors.toList());

        List<BestPlaceDto> bestRestaurants = topRestaurants.stream()
                .map(this::convertToBestPlaceDto)
                .collect(Collectors.toList());
        List<DestinationInfoDto> infoDtos = destination.getInfos().stream()
                .map(info -> DestinationInfoDto.builder()
                        .id(info.getId())
                        .infoKey(info.getInfoKey())
                        .infoValue(info.getInfoValue())
                        .imageUrl(info.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return DestinationDetailDto.builder()
                .id(destination.getId())
                .name(destination.getName())
                .country(destination.getCountry())
                .description(destination.getDescription())
                .lat(destination.getLat())
                .lng(destination.getLon())
                .images(images)
                .bestPlaces(bestPlaces)
                .bestRestaurants(bestRestaurants)
                .infos(infoDtos)
                .build();
    }


    private BestPlaceDto convertToBestPlaceDto(Place place) {
        place.getImages().size();
        String mainImage = place.getImages().stream()
                .findFirst()
                .map(PlaceImage::getImageUrl)
                .orElse(null);

        return BestPlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .rating(place.getRating())
                .lng(place.getLon())
                .lat(place.getLat())
                .description(place.getDescription())
                .mainImage(mainImage)
                .category(CategoryDto.builder()
                        .id(place.getCategory().getId())
                        .name(place.getCategory().getName())
                        .build())
                .build();
    }

    // Place list item + fav
    private PlaceDto convertToPlaceDto(Place place, boolean favorited, long favoriteCount) {
        String mainImage = place.getImages().stream()
                .findFirst()
                .map(PlaceImage::getImageUrl)
                .orElse(null);

        return PlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .lat(place.getLat())
                .lng(place.getLon()) // giữ nguyên lng
                .rating(place.getRating())
                .address(place.getAddress())
                .mainImage(mainImage)
                .description(place.getDescription())
                .favorited(favorited)
                .favoriteCount(favoriteCount)
                .build();
    }

    // Place detail + fav
    private PlaceDetailDto convertToPlaceDetailDto(Place place, boolean favorited, long favoriteCount) {
        java.util.List<ImageDto> images = place.getImages().stream()
                .map(img -> ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .build())
                .toList();

        java.util.List<ReviewDto> reviews = place.getReviews().stream()
                .map(r -> ReviewDto.builder()
                        .id(r.getId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .username(r.getUser().getName())
                        .build())
                .toList();

        return PlaceDetailDto.builder()
                .id(place.getId())
                .name(place.getName())
                .lat(place.getLat())
                .lng(place.getLon()) // giữ nguyên lng
                .description(place.getDescription())
                .rating(place.getRating())
                .address(place.getAddress())
                .website(place.getWebsite())
                .phone(place.getPhone())
                .destination(DestinationDto.builder()
                        .id(place.getDestination().getId())
                        .name(place.getDestination().getName())
                        .build())
                .category(CategoryDto.builder()
                        .id(place.getCategory().getId())
                        .name(place.getCategory().getName())
                        .build())
                .images(images)
                .reviews(reviews)
                // các field mới trong DTO detail:
                .favorited(favorited)
                .favoriteCount(favoriteCount)
                .build();
    }



    private FoodDto convertToFoodDto(Food food) {
        return FoodDto.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .imageUrl(food.getImageUrl())
                .build();
    }
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllOrderByName();
        return categories.stream()
                .map(category -> CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DestinationSummaryDto> searchDestinations(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDestinations();
        }

        List<Destination> destinations = destinationRepository.searchDestinations(keyword.trim());
        return destinations.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }
    private String currentUserIdOrNull() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            String email = auth.getName();
            return userRepository.findByEmail(email).map(User::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Boolean> buildFavoritedMap(String userId, List<String> placeIds) {
        if (userId == null || placeIds == null || placeIds.isEmpty()) return java.util.Collections.emptyMap();
        var likedIds = favoritePlaceRepository.findFavoritedPlaceIds(userId, placeIds);
        var map = new java.util.HashMap<String, Boolean>((int) (likedIds.size() / 0.75) + 1);
        for (String id : likedIds) map.put(id, true);
        return map;
    }

    private Map<String, Long> buildFavoriteCountMap(List<String> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) return java.util.Collections.emptyMap();
        var rows = favoritePlaceRepository.countByPlaceIds(placeIds); // List<Object[]>{ placeId, count }
        var map = new java.util.HashMap<String, Long>((int) (rows.size() / 0.75) + 1);
        for (Object[] r : rows) {
            map.put((String) r[0], (Long) r[1]);
        }
        return map;
    }

}
