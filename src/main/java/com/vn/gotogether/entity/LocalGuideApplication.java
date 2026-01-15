package com.vn.gotogether.entity;

import com.vn.gotogether.dto.localguide.LocalGuideApplicationAdminResponse;
import com.vn.gotogether.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "local_guide_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalGuideApplication {

    @Id
    private String id;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= BASIC INFO =================
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String nationalId;

    private Integer experienceYears;

    // 🔥 ĐỔI từ String → Set<String> (dữ liệu sạch)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "local_guide_application_languages",
            joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "language")
    private Set<String> languages;

    // ================= DOCUMENT PROOF =================
    @Column(nullable = false)
    private String frontImageUrl;

    @Column(nullable = false)
    private String backImageUrl;


    // Optional
    private String portfolioUrl;
    private String certificateUrl;

    // ================= DESTINATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false)
    private String localAddress;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ================= REVIEW / AUDIT =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String reviewerNote;      // Lý do từ chối / ghi chú duyệt

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // ================= RATING (future) =================
    @Builder.Default
    private Double ratingAverage = 0.0;

    @Builder.Default
    private Integer ratingCount = 0;

}


