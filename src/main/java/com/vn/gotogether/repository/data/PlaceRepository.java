package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.destination.id = :destinationId AND p.category.id = :categoryId " +
            "ORDER BY p.rating DESC")
    List<Place> findByDestinationAndCategory(@Param("destinationId") String destinationId,
                                             @Param("categoryId") String categoryId);

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN FETCH p.destination " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.reviews r " +
            "LEFT JOIN FETCH r.user " +
            "WHERE p.id = :placeId")
    Optional<Place> findByIdWithDetails(@Param("placeId") String placeId);


    @Query("SELECT p FROM Place p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.destination.id = :destinationId " +
            "ORDER BY p.rating DESC")
    List<Place> findTop5ByDestinationOrderByRatingDesc(@Param("destinationId") String destinationId, Pageable pageable);

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.destination.id = :destinationId " +
            "AND p.category.id IN ('cat-beach', 'cat-cultural', 'cat-nature', 'cat-temple', 'cat-market') " +
            "ORDER BY p.rating DESC")
    List<Place> findTopAttractionsByDestination(@Param("destinationId") String destinationId, Pageable pageable);

    @Query("SELECT p FROM Place p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.destination.id = :destinationId " +
            "AND p.category.id IN ('cat-restaurant', 'cat-food') " +
            "ORDER BY p.rating DESC")
    List<Place> findTopRestaurantsByDestination(@Param("destinationId") String destinationId, Pageable pageable);

    @Query(value = """
    SELECT p 
    FROM Place p
    WHERE p.destination.id = :destinationId
      AND p.id <> :placeId
    ORDER BY (
        6371 * acos(
            cos(radians(:lat)) * cos(radians(p.lat)) *
            cos(radians(p.lon) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(p.lat))
        ))""")
    List<Place> findNearbyPlaces(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("destinationId") String destinationId,
            @Param("placeId") String placeId,
            Pageable pageable);

}