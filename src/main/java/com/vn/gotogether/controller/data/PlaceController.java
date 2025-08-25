package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.PlaceDto;
import com.vn.gotogether.service.data.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/by-destination/{destinationId}")
    public ResponseEntity<Page<PlaceDto>> getPlacesByDestination(
            @PathVariable String destinationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String categoryId) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PlaceDto> places = placeService.getPlacesByDestination(
                destinationId, categoryId, pageable);

        return ResponseEntity.ok(places);
    }

    @GetMapping("/by-destination-name")
    public ResponseEntity<Page<PlaceDto>> getPlacesByDestinationName(
            @RequestParam String destinationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String categoryId) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PlaceDto> places = placeService.getPlacesByDestinationName(
                destinationName, categoryId, pageable);

        return ResponseEntity.ok(places);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PlaceDto>> searchPlaces(
            @RequestParam(required = false) String destinationName,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PlaceDto> places = placeService.searchPlaces(
                destinationName, categoryId, keyword, minRating, pageable);

        return ResponseEntity.ok(places);
    }
}