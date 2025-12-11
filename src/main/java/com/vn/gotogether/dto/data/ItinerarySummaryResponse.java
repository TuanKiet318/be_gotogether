package com.vn.gotogether.dto.data;

import lombok.*;
import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItinerarySummaryResponse {
    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalItems;
    private String destinationId;
    private String destinationName;
    private String ownerId;
    private String ownerName;
    private String ownerAvatar;
    private boolean isOwner;  // Thêm trường này
}