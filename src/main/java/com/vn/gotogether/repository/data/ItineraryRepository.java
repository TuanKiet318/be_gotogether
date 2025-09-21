package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {

    List<Itinerary> findByUser_IdOrderByCreatedAtDesc(String userId);

    // Lấy ownerId mà không phải load toàn bộ Itinerary (nhanh cho PermissionService)
    @Query("select i.user.id from Itinerary i where i.id = :itineraryId")
    Optional<String> findOwnerIdByItineraryId(String itineraryId);

    // Check “is owner?” one-liner
    boolean existsByIdAndUser_Id(String id, String userId);
}
