package com.vn.gotogether.dto.data;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryFeaturedResponse {

    private String id;
    private String title;
    private String overview;

    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private int totalItems;

    /* destination */
    private String destinationId;
    private String destinationName;

    /* hero */
    private String heroImage; // lấy ảnh đầu tiên

    /* tags */
    private Set<String> tags;

}
