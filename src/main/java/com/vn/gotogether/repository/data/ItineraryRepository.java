package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {

    List<Itinerary> findByUser_IdOrderByCreatedAtDesc(String userId);

    // lấy ownerId nhanh (dùng cho PermissionService)
    @Query("select i.user.id from Itinerary i where i.id = :itineraryId")
    Optional<String> findOwnerIdById(@Param("itineraryId") String itineraryId);


    // check isOwner
    boolean existsByIdAndUser_Id(String id, String userId);

    // ==== mới: dựa trực tiếp trên Itinerary.destination ====

    // featured itineraries
    Page<Itinerary> findByDestination_IdAndIsFeaturedTrue(
            String destinationId,
            Pageable pageable
    );

    // tất cả itineraries theo destination
    Page<Itinerary> findByDestination_Id(
            String destinationId,
            Pageable pageable
    );
}
