package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.PlaceDto;
import com.vn.gotogether.entity.Place;
import com.vn.gotogether.repository.data.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Page<PlaceDto> getPlacesByDestination(String destinationId, String categoryId, Pageable pageable) {
        Page<Place> places = placeRepository.findByDestinationId(destinationId, categoryId, pageable);
        return places.map(this::convertToDto);
    }

    public Page<PlaceDto> getPlacesByDestinationName(String destinationName, String categoryId, Pageable pageable) {
        Page<Place> places = placeRepository.findByDestinationName(destinationName, categoryId, pageable);
        return places.map(this::convertToDto);
    }

    public Page<PlaceDto> searchPlaces(String destinationName, String categoryId,
                                       String keyword, Double minRating, Pageable pageable) {
        Page<Place> places = placeRepository.searchPlaces(destinationName, categoryId, keyword, minRating, pageable);
        return places.map(this::convertToDto);
    }

    private PlaceDto convertToDto(Place place) {
        return PlaceDto.builder()
                .id(place.getId())
                .name(place.getName())
                .lat(place.getLat())
                .lon(place.getLon())
                .description(place.getDescription())
                .rating(place.getRating())
                .address(place.getAddress())
                .website(place.getWebsite())
                .phone(place.getPhone())
                .destination(PlaceDto.DestinationDto.builder()
                        .id(place.getDestination().getId())
                        .name(place.getDestination().getName())
                        .country(place.getDestination().getCountry())
                        .lat(place.getDestination().getLat())
                        .lon(place.getDestination().getLon())
                        .build())
                .category(PlaceDto.CategoryDto.builder()
                        .id(place.getCategory().getId())
                        .name(place.getCategory().getName())
                        .parentId(place.getCategory().getParent() != null ?
                                place.getCategory().getParent().getId() : null)
                        .parentName(place.getCategory().getParent() != null ?
                                place.getCategory().getParent().getName() : null)
                        .build())
                .images(place.getImages() != null ?
                        place.getImages().stream()
                                .map(img -> PlaceDto.PlaceImageDto.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .build())
                                .toList() : null)
                .build();
    }
}