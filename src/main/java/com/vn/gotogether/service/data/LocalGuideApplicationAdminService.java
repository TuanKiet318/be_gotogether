package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.localguide.LocalGuideApplicationAdminResponse;
import com.vn.gotogether.dto.localguide.ReviewLocalGuideApplicationRequest;
import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.enums.ApplicationStatus;
import com.vn.gotogether.repository.data.LocalGuideApplicationRepository;
import com.vn.gotogether.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocalGuideApplicationAdminService {

    private final LocalGuideApplicationRepository repository;
    private final UserRepository userRepository;

    // ================= LIST =================
    public Page<LocalGuideApplicationAdminResponse> getApplications(
            ApplicationStatus status,
            Pageable pageable
    ) {
        Page<LocalGuideApplication> page =
                (status == null)
                        ? repository.findAll(pageable)
                        : repository.findByStatus(status, pageable);

        return page.map(LocalGuideApplicationAdminResponse::from);
    }

    // ================= REVIEW =================
    @Transactional
    public void reviewApplication(
            String applicationId,
            ReviewLocalGuideApplicationRequest request,
            String adminId
    ) {
        LocalGuideApplication app = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Application already reviewed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        app.setStatus(request.getStatus());
        app.setReviewerNote(request.getReviewerNote());
        app.setReviewer(admin);
        app.setReviewedAt(LocalDateTime.now());

    }
}
