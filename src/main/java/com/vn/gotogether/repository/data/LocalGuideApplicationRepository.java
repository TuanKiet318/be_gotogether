package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalGuideApplicationRepository
        extends JpaRepository<LocalGuideApplication, String> {

    // Kiểm tra user đã từng nộp đơn chưa
    boolean existsByUserId(String userId);
    Page<LocalGuideApplication> findByStatus(
            ApplicationStatus status,
            Pageable pageable
    );

    // Lấy đơn theo user
    Optional<LocalGuideApplication> findByUserId(String userId);

    // --- RULE: user chỉ có 1 đơn APPROVED ---
    @Query("""
        SELECT a FROM LocalGuideApplication a
        WHERE a.user.id = :userId AND a.status = com.vn.gotogether.enums.ApplicationStatus.APPROVED
    """)
    Optional<LocalGuideApplication> findApprovedByUser(String userId);

    // --- RULE: user chỉ có 1 đơn PENDING ---
    @Query("""
        SELECT a FROM LocalGuideApplication a
        WHERE a.user.id = :userId AND a.status = com.vn.gotogether.enums.ApplicationStatus.PENDING
    """)
    Optional<LocalGuideApplication> findPendingByUser(String userId);

    // --- LIST theo status (phục vụ admin) ---
    List<LocalGuideApplication> findAllByStatus(ApplicationStatus status);
}
