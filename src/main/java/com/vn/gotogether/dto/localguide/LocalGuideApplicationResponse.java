package com.vn.gotogether.dto.localguide;

import com.vn.gotogether.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LocalGuideApplicationResponse {

    private String id;
    private String userId;

    private String fullName;
    private String nationalId;
    private String frontImageUrl;
    private String backImageUrl;
    private String localAddress;
    private String description;

    private ApplicationStatus status;

    private String reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
