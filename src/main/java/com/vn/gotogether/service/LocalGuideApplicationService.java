package com.vn.gotogether.service;

import com.vn.gotogether.dto.localguide.LocalGuideApplicationCreateRequest;
import com.vn.gotogether.dto.localguide.LocalGuideApplicationResponse;
import com.vn.gotogether.entity.Destination;
import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.enums.ApplicationStatus;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.DestinationRepository;
import com.vn.gotogether.repository.data.LocalGuideApplicationRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalGuideApplicationService {

    private final LocalGuideApplicationRepository repo;
    private final UserRepository userRepo;
    private final DestinationRepository destinationRepo;
    private final MediaService uploadService;

    // -------------------------------------------------------
    // APPLY
    // -------------------------------------------------------
    public LocalGuideApplicationResponse apply(
            LocalGuideApplicationCreateRequest req,
            String frontUrl,
            String backUrl,
            String selfieUrl,
            String portfolioUrl,
            String certificateUrl,
            User user
    ) {
        if (repo.findApprovedByUser(user.getId()).isPresent()) {
            throw new RuntimeException("Bạn đã là Local Guide được duyệt");
        }

        if (repo.findPendingByUser(user.getId()).isPresent()) {
            throw new RuntimeException("Bạn đang có đơn chờ duyệt");
        }

        Destination dest = destinationRepo.findById(req.getDestinationId())
                .orElseThrow(() -> new RuntimeException("Destination không tồn tại"));

        LocalGuideApplication app = LocalGuideApplication.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .destination(dest)
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .nationalId(req.getNationalId())
                .experienceYears(req.getExperienceYears())
                .languages(req.getLanguages())
                .frontImageUrl(frontUrl)
                .backImageUrl(backUrl)
                .selfieWithIdUrl(selfieUrl)
                .portfolioUrl(portfolioUrl)
                .certificateUrl(certificateUrl)
                .localAddress(req.getLocalAddress())
                .description(req.getDescription())
                .status(ApplicationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .ratingAverage(0.0)
                .ratingCount(0)
                .build();

        repo.save(app);
        return LocalGuideApplicationResponse.from(app);
    }




    // -------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------
    public LocalGuideApplicationResponse getById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        LocalGuideApplication app = repo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return LocalGuideApplicationResponse.from(app);
    }


    // -------------------------------------------------------
    // LIST BY STATUS (admin)
    // -------------------------------------------------------
    public List<LocalGuideApplicationResponse> getList(ApplicationStatus status) {
        List<LocalGuideApplication> list;

        if (status != null) {
            list = repo.findAllByStatus(status);
        } else {
            list = repo.findAll();
        }

        return list.stream()
                .map(LocalGuideApplicationResponse::from)
                .toList();
    }
    public LocalGuideApplicationResponse verify(String id, boolean approved, String reviewerNote) {

        LocalGuideApplication app = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Chỉ duyệt đơn đang trong trạng thái PENDING.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User reviewer = userRepo.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));


        app.setStatus(approved ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        app.setReviewer(reviewer);
        app.setReviewerNote(reviewerNote);
        app.setReviewedAt(LocalDateTime.now());

        repo.save(app);

        return LocalGuideApplicationResponse.from(app);
    }

    public boolean checkIfUserIsLocalGuide(String userId) {
        return repo.findApprovedByUser(userId).isPresent();
    }

    public LocalGuideApplicationResponse updateApplication(
            String id,
            LocalGuideApplicationCreateRequest req,
            User user // lấy từ authentication ở controller
    ) {

        LocalGuideApplication app = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Chỉ được cập nhật đơn khi đang ở trạng thái PENDING.");
        }

        // Không được sửa của người khác
        if (!app.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa đơn của người khác.");
        }

        Destination dest = destinationRepo.findById(req.getDestinationId())
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        // ------------------------------
        // UPLOAD NEW FILES (nếu có)
        // ------------------------------
        String frontUrl = app.getFrontImageUrl();
        if (req.getFrontImageFile() != null && !req.getFrontImageFile().isEmpty()) {
            frontUrl = uploadService.uploadMedia(req.getFrontImageFile(), user.getId(), "guide");
        }

        String backUrl = app.getBackImageUrl();
        if (req.getBackImageFile() != null && !req.getBackImageFile().isEmpty()) {
            backUrl = uploadService.uploadMedia(req.getBackImageFile(), user.getId(), "guide");
        }

        String portfolioUrl = app.getPortfolioUrl();
        if (req.getPortfolioFile() != null && !req.getPortfolioFile().isEmpty()) {
            portfolioUrl = uploadService.uploadMedia(req.getPortfolioFile(), user.getId(), "guide");
        }

        String certificateUrl = app.getCertificateUrl();
        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            certificateUrl = uploadService.uploadMedia(req.getCertificateFile(), user.getId(), "guide");
        }

        // ------------------------------
        // UPDATE FIELDS
        // ------------------------------
        app.setFullName(req.getFullName());
        app.setPhone(req.getPhone());
        app.setNationalId(req.getNationalId());
        app.setExperienceYears(req.getExperienceYears());
        app.setLanguages(req.getLanguages());
        app.setLocalAddress(req.getLocalAddress());
        app.setDescription(req.getDescription());
        app.setDestination(dest);

        // Update URLs uploaded
        app.setFrontImageUrl(frontUrl);
        app.setBackImageUrl(backUrl);
        app.setPortfolioUrl(portfolioUrl);
        app.setCertificateUrl(certificateUrl);

        repo.save(app);
        return LocalGuideApplicationResponse.from(app);
    }


    // -------------------------------------------------------
    // ADMIN REVIEW
    // -------------------------------------------------------
    public LocalGuideApplicationResponse review(String id, boolean approved, String reviewerId) {

        LocalGuideApplication app = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Chỉ được duyệt đơn đang ở trạng thái PENDING.");
        }

        User reviewer = userRepo.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        app.setStatus(approved ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        app.setReviewer(reviewer);
        app.setReviewedAt(LocalDateTime.now());

        repo.save(app);
        return LocalGuideApplicationResponse.from(app);
    }


    // -------------------------------------------------------
    // DELETE (ADMIN)
    // -------------------------------------------------------
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Application not found");
        }
        repo.deleteById(id);
    }


    // -------------------------------------------------------
    // ADD RATING
    // -------------------------------------------------------
    public LocalGuideApplicationResponse addRating(String id, int rating) {

        LocalGuideApplication app = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new RuntimeException("Chỉ hướng dẫn viên đã được duyệt mới nhận đánh giá.");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating phải nằm trong khoảng 1–5.");
        }

        // Update rating
        double total = app.getRatingAverage() * app.getRatingCount();
        total += rating;

        app.setRatingCount(app.getRatingCount() + 1);
        app.setRatingAverage(total / app.getRatingCount());

        repo.save(app);

        return LocalGuideApplicationResponse.from(app);
    }
}
