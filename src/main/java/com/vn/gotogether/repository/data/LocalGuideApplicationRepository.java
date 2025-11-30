package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalGuideApplicationRepository extends JpaRepository<LocalGuideApplication, String> {

    Optional<LocalGuideApplication> findFirstByUser_IdAndStatus(String userId, ApplicationStatus status);

    List<LocalGuideApplication> findByStatus(ApplicationStatus status);
}
