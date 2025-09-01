package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.DestinationResponseDto;
import com.vn.gotogether.entity.Destination;
import com.vn.gotogether.repository.data.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public List<DestinationResponseDto> getAllDestinations() {
        List<Destination> destinations = destinationRepository.findAllOrderByName();
        return destinations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<DestinationResponseDto> getDestinationsByCountry(String country) {
        List<Destination> destinations = destinationRepository.findByCountryOrderByName(country);
        return destinations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DestinationResponseDto convertToDto(Destination destination) {
        return DestinationResponseDto.builder()
                .id(destination.getId())
                .name(destination.getName())
                .country(destination.getCountry())
                .description(destination.getDescription())
                .lat(destination.getLat())
                .lon(destination.getLon())
                .totalPlaces(destination.getPlaces() != null ? destination.getPlaces().size() : 0)
                .build();
    }
}