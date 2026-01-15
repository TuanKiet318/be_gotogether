package com.vn.gotogether.dto.localguide;

import com.vn.gotogether.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewLocalGuideApplicationRequest {

    @NotNull
    private ApplicationStatus status; // APPROVED / REJECTED

    private String reviewerNote;
}
