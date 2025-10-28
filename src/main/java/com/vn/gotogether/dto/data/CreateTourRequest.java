package com.vn.gotogether.dto.data;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTourRequest {
    private String itineraryId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private Integer maxParticipants;
    private BigDecimal pricePerPerson;
}