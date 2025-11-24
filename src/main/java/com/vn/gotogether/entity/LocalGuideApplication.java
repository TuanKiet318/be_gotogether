package com.vn.gotogether.entity;

import com.vn.gotogether.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "local_guide_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalGuideApplication {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;              // Người nộp đơn

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "national_id", nullable = false)
    private String nationalId;

    @Column(name = "front_image_url", nullable = false)
    private String frontImageUrl;

    @Column(name = "back_image_url", nullable = false)
    private String backImageUrl;

    @Column(name = "local_address", nullable = false)
    private String localAddress;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;           // Admin duyệt
}

