// src/main/java/com/vn/gotogether/dto/data/CloneItineraryRequest.java
package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CloneItineraryRequest {

    /**
     * Tiêu đề mới cho lịch trình clone.
     * Nếu null/blank => dùng tiêu đề cũ + " (Bản sao)".
     */
    private String title;

    /**
     * Khoảng ngày mới cho lịch trình clone.
     * Nếu null => dùng đúng ngày cũ.
     */
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Có copy item không. Mặc định true.
     */
    @Builder.Default
    private Boolean includeItems = true;

    /**
     * Nếu true (mặc định): cắt bớt các item có dayNumber > số ngày mới.
     * Nếu false: ném lỗi nếu gặp item nằm ngoài phạm vi số ngày mới.
     */
    @Builder.Default
    private Boolean trimItemsExceedingNewRange = true;
}
