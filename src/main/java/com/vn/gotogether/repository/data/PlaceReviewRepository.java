package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.PlaceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceReviewRepository extends JpaRepository<PlaceReview, String> {
    List<PlaceReview> findByPlaceId(String placeId);
    boolean existsByPlaceIdAndUserId(String placeId, String userId);
}
