package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {

    // Lấy places theo destination ID
    @Query("SELECT p FROM Place p " +
            "JOIN FETCH p.destination d " +
            "JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE d.id = :destinationId " +
            "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<Place> findByDestinationId(
            @Param("destinationId") String destinationId,
            @Param("categoryId") String categoryId,
            Pageable pageable);

    // Lấy places theo tên destination
    @Query("SELECT p FROM Place p " +
            "JOIN FETCH p.destination d " +
            "JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :destinationName, '%')) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<Place> findByDestinationName(
            @Param("destinationName") String destinationName,
            @Param("categoryId") String categoryId,
            Pageable pageable);

    // Tìm kiếm places với nhiều tiêu chí
    @Query("SELECT p FROM Place p " +
            "JOIN FETCH p.destination d " +
            "JOIN FETCH p.category c " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE (:destinationName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :destinationName, '%'))) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:keyword IS NULL OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minRating IS NULL OR p.rating >= :minRating)")
    Page<Place> searchPlaces(
            @Param("destinationName") String destinationName,
            @Param("categoryId") String categoryId,
            @Param("keyword") String keyword,
            @Param("minRating") Double minRating,
            Pageable pageable);

    // Đếm số lượng places theo destination
    @Query("SELECT COUNT(p) FROM Place p WHERE p.destination.id = :destinationId")
    long countByDestinationId(@Param("destinationId") String destinationId);

    // Lấy places theo category trong một destination
    @Query("SELECT p FROM Place p " +
            "JOIN FETCH p.destination d " +
            "JOIN FETCH p.category c " +
            "WHERE d.id = :destinationId AND c.id = :categoryId")
    Page<Place> findByDestinationIdAndCategoryId(
            @Param("destinationId") String destinationId,
            @Param("categoryId") String categoryId,
            Pageable pageable);
}