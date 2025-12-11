package com.vn.gotogether.dto;

import java.util.Set;

// LocalGuideCreateRequest
public record LocalGuideCreateRequest(
        String userId,
        String fullName,
        String avatarUrl,
        String bio,
        String locationBase,
        Integer experienceYears,
        Set<String> languages,
        Set<String> skills,
        Set<String> specialties,
        Set<String> serviceAreas,
        Double pricePerHour,
        Double pricePerTour,
        Integer maxGroupSize
) {}
