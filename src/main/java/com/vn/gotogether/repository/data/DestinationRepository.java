package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, String> {

    @Query("SELECT d FROM Destination d LEFT JOIN FETCH d.images ORDER BY d.name")
    List<Destination> findAllWithImages();

    @Query("SELECT d FROM Destination d " +
            "LEFT JOIN FETCH d.images " +
            "LEFT JOIN FETCH d.infos " +
            "WHERE d.id = :destinationId")
    Optional<Destination> findByIdWithDetails(@Param("destinationId") String destinationId);

    @Query("SELECT COUNT(p) FROM Place p WHERE p.destination.id = :destinationId")
    Integer countPlacesByDestinationId(@Param("destinationId") String destinationId);

    @Query("SELECT COUNT(f) FROM Food f WHERE f.destination.id = :destinationId")
    Integer countFoodsByDestinationId(@Param("destinationId") String destinationId);

    @Query("SELECT d FROM Destination d " +
            "LEFT JOIN FETCH d.images " +
            "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.country) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY d.name")
    List<Destination> searchDestinations(@Param("keyword") String keyword);

    @Query("""
        SELECT d 
        FROM Destination d
        LEFT JOIN d.places p
        GROUP BY d
        ORDER BY COUNT(p) DESC
    """)
    List<Destination> findTopDestinations(Pageable pageable);

    // Query tìm kiếm với phân trang
    @Query("SELECT DISTINCT d FROM Destination d " +
            "LEFT JOIN FETCH d.images " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "       LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "       LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Destination> searchDestinations(
            @Param("search") String search,
            Pageable pageable);
}