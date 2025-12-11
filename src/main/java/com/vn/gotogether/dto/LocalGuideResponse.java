package com.vn.gotogether.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record LocalGuideResponse(
        String id,
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
        Integer maxGroupSize,
        Boolean available,
        Double ratingAverage,
        Integer reviewCount,
        Integer completedTours,
        Boolean verified,
        List<String> albumPhotos,
        Map<String,String> socialLinks,
        LocalDateTime lastActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}