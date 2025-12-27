package com.vn.gotogether.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.model.ContentItem;
import com.vn.gotogether.repository.data.*;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DestinationService {
    @Autowired
    private ObjectMapper objectMapper;
    private final DestinationRepository destinationRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceRepository placeRepository;
    private final FoodRepository foodRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;
    private final FoodPlaceRepository foodPlaceRepository;


    public DestinationService(DestinationRepository destinationRepository,
                              CategoryRepository categoryRepository,
                              PlaceRepository placeRepository,
                              FoodRepository foodRepository,
                              FavoritePlaceRepository favoritePlaceRepository,
                              UserRepository userRepository, FoodPlaceRepository foodPlaceRepository) {
        this.destinationRepository = destinationRepository;
        this.categoryRepository = categoryRepository;
        this.placeRepository = placeRepository;
        this.foodRepository = foodRepository;
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.userRepository = userRepository;
        this.foodPlaceRepository = foodPlaceRepository;
    }

    public List<DestinationSummaryDto> getAllDestinations() {
        List<Destination> destinations = destinationRepository.findAllWithImages();
        return destinations.stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    public List<DestinationSummaryDto> getTop6FeaturedDestinations() {
        Pageable pageable = PageRequest.of(0, 6);
        List<Destination> destinations =
                destinationRepository.findTopDestinations(pageable);

        return destinations.stream()
                .map(this::convertToSummaryDto)
                .toList();
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

    private FoodDto convertToFoodDto(Food food) {
        List<ContentItem> contentItems = null;
        if (food.getContent() != null) {
            try {
                contentItems = objectMapper.readValue(
                        food.getContent(),
                        new TypeReference<List<ContentItem>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing food content JSON", e);
            }
        }

        return FoodDto.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .imageUrl(food.getImageUrl())
                .content(contentItems)
                .build();
    }

    public FoodDto getFoodDetail(String id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Food not found with id: " + id));
        return convertToFoodDto(food);
    }

    public PlacesResponseDto getRestaurantsByFood(String foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new EntityNotFoundException("Food not found with id: " + foodId));

        // Lấy danh sách places có liên kết food này và category = restaurant
        List<Place> places = foodPlaceRepository.findRestaurantsByFoodId(foodId);

        List<String> placeIds = places.stream()
                .map(Place::getId)
                .collect(Collectors.toList());

        // User hiện tại (có thể null nếu chưa login)
        String userId = currentUserIdOrNull();

        // batch maps
        final Map<String, Boolean> favMap = buildFavoritedMap(userId, placeIds);
        final Map<String, Long> favCountMap = buildFavoriteCountMap(placeIds);

        // Map entity → dto
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
                            .lng(p.getLon()) // giữ nguyên
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
                        .id("restaurant")   // fix cứng category restaurant
                        .name("Nhà hàng")
                        .build())
                .destination(DestinationDto.builder()
                        .id(food.getDestination().getId())
                        .name(food.getDestination().getName())
                        .build())
                .places(placeDtos)
                .total(placeDtos.size())
                .build();
    }

    public Page<PlaceDto> getPlacesByDestination(
            String destinationId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Place> places = placeRepository.findAllByDestination(destinationId, pageable);

        return places.map(PlaceDto::new);
    }

    public PlacesResponseDto searchPlacesInDestination(String destinationId, String keyword) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination not found with id: " + destinationId));

        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu không có từ khóa => trả tất cả địa điểm
            List<Place> allPlaces = placeRepository.findAllByDestination(destinationId, PageRequest.of(0, 100)).getContent();
            List<PlaceDto> allDtos = allPlaces.stream()
                    .map(p -> convertToPlaceDto(p, false, 0))
                    .toList();
            return PlacesResponseDto.builder()
                    .destination(DestinationDto.builder()
                            .id(destination.getId())
                            .name(destination.getName())
                            .build())
                    .places(allDtos)
                    .total(allDtos.size())
                    .build();
        }

        List<Place> results = placeRepository.searchPlacesInDestination(destinationId, keyword.trim());

        List<String> placeIds = results.stream().map(Place::getId).toList();
        String userId = currentUserIdOrNull();
        final Map<String, Boolean> favMap = buildFavoritedMap(userId, placeIds);
        final Map<String, Long> favCountMap = buildFavoriteCountMap(placeIds);

        List<PlaceDto> placeDtos = results.stream()
                .map(p -> convertToPlaceDto(
                        p,
                        favMap.getOrDefault(p.getId(), false),
                        favCountMap.getOrDefault(p.getId(), 0L)
                ))
                .toList();

        return PlacesResponseDto.builder()
                .destination(DestinationDto.builder()
                        .id(destination.getId())
                        .name(destination.getName())
                        .build())
                .places(placeDtos)
                .total(placeDtos.size())
                .build();
    }

    public PlacesByCategoryResponseDto getNearestPlacesByCategoriesFromPlace(String placeId) {

        Place sourcePlace = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Place not found with id: " + placeId));

        // Lấy tất cả places sắp xếp theo khoảng cách từ sourcePlace
        List<Place> allPlaces = placeRepository.findNearestPlacesByCategoriesFromPlace(
                sourcePlace.getLat(),
                sourcePlace.getLon(),
                sourcePlace.getDestination().getId(),
                placeId
        );

        // Lọc lấy 1 place gần nhất cho mỗi category
        Map<String, Place> categoryMap = new java.util.LinkedHashMap<>();
        for (Place place : allPlaces) {
            String categoryId = place.getCategory().getId();
            if (!categoryMap.containsKey(categoryId)) {
                categoryMap.put(categoryId, place);
            }
        }

        List<Place> nearestPlaces = new java.util.ArrayList<>(categoryMap.values());

        // Batch favorite
        List<String> placeIds = nearestPlaces.stream()
                .map(Place::getId)
                .collect(Collectors.toList());

        String userId = currentUserIdOrNull();
        final Map<String, Boolean> favMap = buildFavoritedMap(userId, placeIds);
        final Map<String, Long> favCountMap = buildFavoriteCountMap(placeIds);

        // Map sang CategoryPlacesDto (nhóm theo category)
        List<CategoryPlacesDto> categoryPlacesList = nearestPlaces.stream()
                .map(p -> {
                    PlaceDto placeDto = convertToPlaceDto(
                            p,
                            favMap.getOrDefault(p.getId(), false),
                            favCountMap.getOrDefault(p.getId(), 0L)
                    );

                    // Tính khoảng cách (optional)
                    double distance = calculateDistance(
                            sourcePlace.getLat(),
                            sourcePlace.getLon(),
                            p.getLat(),
                            p.getLon()
                    );

                    return CategoryPlacesDto.builder()
                            .category(CategoryDto.builder()
                                    .id(p.getCategory().getId())
                                    .name(p.getCategory().getName())
                                    .build())
                            .nearestPlace(placeDto)
                            .distance(Math.round(distance * 100.0) / 100.0) // làm tròn 2 chữ số
                            .build();
                })
                .collect(Collectors.toList());

        return PlacesByCategoryResponseDto.builder()
                .destination(DestinationDto.builder()
                        .id(sourcePlace.getDestination().getId())
                        .name(sourcePlace.getDestination().getName())
                        .build())
                .categoryPlaces(categoryPlacesList)
                .totalCategories(categoryPlacesList.size())
                .totalPlaces(categoryPlacesList.size())
                .build();
    }

    // Helper method tính khoảng cách
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
