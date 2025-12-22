package com.vn.gotogether.dto.localguide;

import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class LocalGuideApplicationResponse {

    private String id;
    private String userId;

    // ================= BASIC INFO =================
    private String fullName;
    private String phone;
    private String nationalId;
    private Integer experienceYears;

    // 🔥 Dữ liệu sạch
    private Set<String> languages;

    // ================= DOCUMENTS =================
    private String frontImageUrl;
    private String backImageUrl;
    private String selfieWithIdUrl;

    private String portfolioUrl;
    private String certificateUrl;

    // ================= DESTINATION =================
    private String destinationId;
    private String destinationName;

    private String localAddress;
    private String description;

    // ================= REVIEW / STATUS =================
    private ApplicationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    private String reviewerId;
    private String reviewerNote;

    // ================= RATING =================
    private Double ratingAverage;
    private Integer ratingCount;

    // ==================================================
    //                    MAPPER
    // ==================================================
    public static LocalGuideApplicationResponse from(LocalGuideApplication e) {
        return LocalGuideApplicationResponse.builder()
                .id(e.getId())
                .userId(e.getUser().getId())

                .fullName(e.getFullName())
                .phone(e.getPhone())
                .nationalId(e.getNationalId())
                .experienceYears(e.getExperienceYears())
                .languages(e.getLanguages())

                .frontImageUrl(e.getFrontImageUrl())
                .backImageUrl(e.getBackImageUrl())
                .selfieWithIdUrl(e.getSelfieWithIdUrl())
                .portfolioUrl(e.getPortfolioUrl())
                .certificateUrl(e.getCertificateUrl())

                .destinationId(e.getDestination().getId())
                .destinationName(e.getDestination().getName())

                .localAddress(e.getLocalAddress())
                .description(e.getDescription())

                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .reviewedAt(e.getReviewedAt())

                .reviewerId(e.getReviewer() != null ? e.getReviewer().getId() : null)
                .reviewerNote(e.getReviewerNote())

                .ratingAverage(e.getRatingAverage())
                .ratingCount(e.getRatingCount())
                .build();
    }
}
