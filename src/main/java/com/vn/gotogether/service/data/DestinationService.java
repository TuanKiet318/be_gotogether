package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.repository.data.CategoryRepository;
import com.vn.gotogether.repository.data.DestinationRepository;
import com.vn.gotogether.repository.data.FoodRepository;
import com.vn.gotogether.repository.data.PlaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final CategoryRepository categoryRepository;
    private final PlaceRepository placeRepository;
    private final FoodRepository foodRepository;

    public DestinationService(DestinationRepository destinationRepository,
                              CategoryRepository categoryRepository,
                              PlaceRepository placeRepository,
                              FoodRepository foodRepository) {
        this.destinationRepository = destinationRepository;
        this.categoryRepository = categoryRepository;
        this.placeRepository = placeRepository;
        this.foodRepository = foodRepository;
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
        List<PlaceDto> placeDtos = places.stream()
                .map(this::convertToPlaceDto)
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

        return convertToPlaceDetailDto(place);
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

        List<Place> topPlaces = placeRepository.findTop5ByDestinationOrderByRatingDesc(
                destination.getId(), PageRequest.of(0, 5));

        List<BestPlaceDto> bestPlaces = topPlaces.stream()
                .map(place -> {
                    String mainImage = place.getImages().stream()
                            .findFirst()
                            .map(PlaceImage::getImageUrl)
                            .orElse(null);

                    return BestPlaceDto.builder()
                            .id(place.getId())
                            .name(place.getName())
                            .rating(place.getRating())
                            .mainImage(mainImage)
                            .category(CategoryDto.builder()
                                    .id(place.getCategory().getId())
                                    .name(place.getCategory().getName())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());

        return DestinationDetailDto.builder()
                .id(destination.getId())
                .name(destination.getName())
                .country(destination.getCountry())
                .description(destination.getDescription())
                .lat(destination.getLat())
                .lon(destination.getLon())
                .images(images)
                .bestPlaces(bestPlaces)
                .build();
    }

    private PlaceDto convertToPlaceDto(Place place) {
        String mainImage = place.getImages().stream()
                .findFirst()
                .map(PlaceImage::getImageUrl)
                .orElse(null);

        return PlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .lat(place.getLat())
                .lon(place.getLon())
                .rating(place.getRating())
                .address(place.getAddress())
                .mainImage(mainImage)
                .description(place.getDescription())
                .build();
    }

    private PlaceDetailDto convertToPlaceDetailDto(Place place) {
        List<ImageDto> images = place.getImages().stream()
                .map(img -> ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return PlaceDetailDto.builder()
                .id(place.getId())
                .name(place.getName())
                .lat(place.getLat())
                .lon(place.getLon())
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
}
