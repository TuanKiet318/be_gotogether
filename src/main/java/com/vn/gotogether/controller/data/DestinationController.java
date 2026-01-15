package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.ApiResponse;
import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.Food;
import com.vn.gotogether.service.data.DestinationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    // 1. GET ALL DESTINATIONS
    @GetMapping("/destinations")
    public ApiResponse<PagedResponse<DestinationSummaryDto>> getDestinations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "9") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        try {
            DestinationSearchRequest request = new DestinationSearchRequest();
            request.setSearch(search);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);

            PagedResponse<DestinationSummaryDto> response = destinationService.searchDestinations(request);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/destinations/all")
    public ApiResponse<List<DestinationSummaryDto>> getAllDestinations() {
        try {
            // Tạo request với size lớn để lấy tất cả
            DestinationSearchRequest request = new DestinationSearchRequest();
            request.setPage(0);
            request.setSize(1000); // Số lớn để lấy hết

            PagedResponse<DestinationSummaryDto> pagedResponse = destinationService.searchDestinations(request);
            return ApiResponse.success(pagedResponse.getContent());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }
    // 2. GET DESTINATION DETAIL
    @GetMapping("/destinations/{destinationId}")
    public ApiResponse<DestinationDetailDto> getDestinationDetail(@PathVariable String destinationId) {
        try {
            DestinationDetailDto destination = destinationService.getDestinationDetail(destinationId);
            return ApiResponse.success(destination);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 3. GET CATEGORIES BY DESTINATION
    @GetMapping("/destinations/{destinationId}/categories")
    public ApiResponse<CategoriesResponseDto> getCategoriesByDestination(@PathVariable String destinationId) {
        try {
            CategoriesResponseDto response = destinationService.getCategoriesByDestination(destinationId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 4. GET PLACES BY CATEGORY
    @GetMapping("/destinations/{destinationId}/categories/{categoryId}/places")
    public ApiResponse<PlacesResponseDto> getPlacesByCategory(
            @PathVariable String destinationId,
            @PathVariable String categoryId) {
        try {
            PlacesResponseDto response = destinationService.getPlacesByCategory(destinationId, categoryId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 5. GET FOODS BY DESTINATION
    @GetMapping("/destinations/{destinationId}/foods")
    public ApiResponse<FoodsResponseDto> getFoodsByDestination(@PathVariable String destinationId) {
        try {
            FoodsResponseDto response = destinationService.getFoodsByDestination(destinationId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 6. GET PLACE DETAIL
    @GetMapping("/places/{placeId}")
    public ApiResponse<PlaceDetailDto> getPlaceDetail(@PathVariable String placeId) {
        try {
            PlaceDetailDto place = destinationService.getPlaceDetail(placeId);
            return ApiResponse.success(place);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 7. GET ALL CATEGORIES
    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> getAllCategories() {
        try {
            List<CategoryDto> categories = destinationService.getAllCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    // 8. SEARCH DESTINATIONS
    @GetMapping("/destinations/search")
    public ApiResponse<List<DestinationSummaryDto>> searchDestinations(
            @RequestParam(value = "q", required = false) String keyword) {
        try {
            List<DestinationSummaryDto> destinations = destinationService.searchDestinations(keyword);
            return ApiResponse.success(destinations);
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/foods/{id}")
    public ResponseEntity<FoodDto> getFoodDetail(@PathVariable String id) {
        return ResponseEntity.ok(destinationService.getFoodDetail(id));
    }

    @GetMapping("foods/{foodId}/restaurants")
    public ResponseEntity<PlacesResponseDto> getRestaurantsByFood(@PathVariable String foodId) {
        return ResponseEntity.ok(destinationService.getRestaurantsByFood(foodId));
    }

    @GetMapping("/by-destination/{destinationId}/places")
    public ApiResponse<Page<PlaceDto>> getPlacesByDestination(
            @PathVariable String destinationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        try {
            Page<PlaceDto> places = destinationService.getPlacesByDestination(
                    destinationId, page, size, sortBy, sortDirection
            );
            return ApiResponse.success(places);
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/destinations/{destinationId}/places/search")
    public ApiResponse<PlacesResponseDto> searchPlacesInDestination(
            @PathVariable String destinationId,
            @RequestParam(value = "q", required = false) String keyword) {
        try {
            PlacesResponseDto response = destinationService.searchPlacesInDestination(destinationId, keyword);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/places/{placeId}/nearest-by-categories")
    public ApiResponse<PlacesByCategoryResponseDto> getNearestPlacesByCategoriesFromPlace(
            @PathVariable String placeId) {
        try {
            PlacesByCategoryResponseDto response = destinationService.getNearestPlacesByCategoriesFromPlace(placeId);
            return ApiResponse.success(response);
        } catch (EntityNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/destinations/featured")
    public ApiResponse<List<DestinationSummaryDto>> getFeaturedDestinations() {
        try {
            List<DestinationSummaryDto> destinations =
                    destinationService.getTop6FeaturedDestinations();
            return ApiResponse.success(destinations);
        } catch (Exception e) {
            return ApiResponse.error(500, "Internal server error: " + e.getMessage());
        }
    }
}
