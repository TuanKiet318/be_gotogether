package com.vn.gotogether.dto.localguide;

import com.vn.gotogether.entity.LocalGuideApplication;
import com.vn.gotogether.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class LocalGuideApplicationAdminResponse {

    private String id;
    private String userId;
    private String fullName;
    private String phone;
    private String nationalId;
    private Integer experienceYears;
    private Set<String> languages;

    private String destinationId;
    private String destinationName;

    private String localAddress;
    private String description;

    private ApplicationStatus status;
    private String reviewerNote;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    public static LocalGuideApplicationAdminResponse from(LocalGuideApplication e) {
        return LocalGuideApplicationAdminResponse.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .fullName(e.getFullName())
                .phone(e.getPhone())
                .nationalId(e.getNationalId())
                .experienceYears(e.getExperienceYears())
                .languages(e.getLanguages())
                .destinationId(e.getDestination().getId())
                .destinationName(e.getDestination().getName())
                .localAddress(e.getLocalAddress())
                .description(e.getDescription())
                .status(e.getStatus())
                .reviewerNote(e.getReviewerNote())
                .createdAt(e.getCreatedAt())
                .reviewedAt(e.getReviewedAt())
                .build();
    }
}

