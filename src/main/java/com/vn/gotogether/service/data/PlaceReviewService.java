package com.vn.gotogether.service.data;

import com.vn.gotogether.entity.PlaceReview;
import com.vn.gotogether.repository.data.PlaceRepository;
import com.vn.gotogether.repository.data.PlaceReviewRepository;
import com.vn.gotogether.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceReviewService {

    private final PlaceReviewRepository placeReviewRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public List<PlaceReview> getReviewsByPlace(String placeId) {
        return placeReviewRepository.findByPlaceId(placeId);
    }

    public PlaceReview addReview(String placeId, String userId, int rating, String comment) {
        var place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found"));
            var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyReviewed = placeReviewRepository.existsByPlaceIdAndUserId(placeId, userId);
        if (alreadyReviewed) {
            throw new RuntimeException("Bạn đã đánh giá địa điểm này rồi!");
        }

        PlaceReview review = PlaceReview.builder()
                .id(UUID.randomUUID().toString())
                .place(place)
                .user(user)
                .rating(rating)
                .comment(comment)
                .build();

        return placeReviewRepository.save(review);
    }

    public PlaceReview updateReview(String reviewId, int rating, String comment) {
        PlaceReview review = placeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setRating(rating);
        review.setComment(comment);
        return placeReviewRepository.save(review);
    }

    public void deleteReview(String reviewId) {
        if (!placeReviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }
        placeReviewRepository.deleteById(reviewId);
    }
}
