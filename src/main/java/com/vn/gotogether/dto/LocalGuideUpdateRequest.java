package com.vn.gotogether.dto;

import lombok.Data;

import java.util.Set;

@Data
public class LocalGuideUpdateRequest {
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String locationBase;
    private Integer experienceYears;
    private Set<String> languages;
    private Set<String> skills;
    private Set<String> specialties;
    private Set<String> serviceAreas;
    private Double pricePerHour;
    private Double pricePerTour;
    private Integer maxGroupSize;
    private Boolean available;
    // getters/setters
}
