package com.vn.gotogether.controller.data;

import com.vn.gotogether.entity.PlaceReview;
import com.vn.gotogether.service.data.PlaceReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PlaceReviewController {

    private final PlaceReviewService placeReviewService;

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<PlaceReview>> getReviewsByPlace(@PathVariable String placeId) {
        return ResponseEntity.ok(placeReviewService.getReviewsByPlace(placeId));
    }

    @PostMapping("/place/{placeId}/user/{userId}")
    public ResponseEntity<PlaceReview> addReview(
            @PathVariable String placeId,
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        int rating = (int) request.get("rating");
        String comment = (String) request.get("comment");
        return ResponseEntity.ok(placeReviewService.addReview(placeId, userId, rating, comment));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<PlaceReview> updateReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> request) {
        int rating = (int) request.get("rating");
        String comment = (String) request.get("comment");
        return ResponseEntity.ok(placeReviewService.updateReview(reviewId, rating, comment));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        placeReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
