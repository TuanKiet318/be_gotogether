package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.FoodResponseDto;
import com.vn.gotogether.service.data.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@CrossOrigin
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponseDto>> getAllFoods() {
        List<FoodResponseDto> foods = foodService.getAllFoods();
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<FoodResponseDto>> getFoodsByDestination(
            @PathVariable String destinationId) {
        List<FoodResponseDto> foods = foodService.getFoodsByDestination(destinationId);
        return ResponseEntity.ok(foods);
    }
}
