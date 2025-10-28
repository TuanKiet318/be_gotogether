package com.vn.gotogether.dto.data;

import com.vn.gotogether.model.TourStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailResponse {
    private String id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal pricePerPerson;
    private TourStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ItineraryInfo itinerary;
    private UserInfo creator;
    private List<ParticipantInfo> participants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryInfo {
        private String id;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String name;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private String id;
        private UserInfo user;
        private LocalDateTime joinedAt;
    }
}