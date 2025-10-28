package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Tour;
import com.vn.gotogether.model.TourStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tour t WHERE t.id = :id")
    Optional<Tour> findByIdWithLock(@Param("id") String id);

    @Query("SELECT t FROM Tour t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:creatorId IS NULL OR t.creator.id = :creatorId) AND " +
            "(:startDateFrom IS NULL OR t.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR t.startDate <= :startDateTo)")
    Page<Tour> findWithFilters(
            @Param("status") TourStatus status,
            @Param("creatorId") String creatorId,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo,
            Pageable pageable);
}
