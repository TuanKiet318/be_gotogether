package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.FoodResponseDto;
import com.vn.gotogether.entity.Food;
import com.vn.gotogether.repository.data.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;

    public List<FoodResponseDto> getAllFoods() {
        List<Food> foods = foodRepository.findAllWithDestination();
        return foods.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FoodResponseDto> getFoodsByDestination(String destinationId) {
        List<Food> foods = foodRepository.findByDestinationIdWithDestination(destinationId);
        return foods.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FoodResponseDto convertToDto(Food food) {
        return FoodResponseDto.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .imageUrl(food.getImageUrl())
                .destinationId(food.getDestination().getId())
                .destinationName(food.getDestination().getName())
                .build();
    }
}
