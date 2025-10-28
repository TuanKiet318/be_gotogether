package com.vn.gotogether.dto.data;

import com.vn.gotogether.model.TourStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer maxParticipants;
    private BigDecimal pricePerPerson;
    private TourStatus status;
}