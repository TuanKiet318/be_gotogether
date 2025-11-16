package com.vn.gotogether.service;

import org.springframework.stereotype.Component;

/**
 * Haversine-based travel time estimator.
 * Single concrete class (no interface) as requested.
 *
 * Usage: inject HaversineTravelTimeProvider directly into services that need travel estimates.
 */
@Component
public class HaversineTravelTimeProvider {

    /**
     * Estimate travel minutes between two coordinates.
     * mode may be null or values like "CAR", "WALK", "BIKE", etc.
     */
    public int estimateTravelMinutes(Double fromLat, Double fromLon, Double toLat, Double toLon, String mode) {
        if (fromLat == null || fromLon == null || toLat == null || toLon == null) return 0;
        double km = haversineKm(fromLat, fromLon, toLat, toLon);
        double speedKmh = speedForMode(mode);
        if (speedKmh <= 0) speedKmh = 30.0;
        double hours = km / speedKmh;
        int minutes = (int) Math.ceil(hours * 60.0);
        return Math.max(1, minutes);
    }

    private double speedForMode(String mode) {
        if (mode == null) return 30.0;
        switch (mode.toUpperCase()) {
            case "WALK": return 5.0;
            case "BIKE": return 12.0;
            case "CAR": return 30.0;
            case "BUS": return 20.0;
            case "TRAIN": return 60.0;
            case "BOAT": return 25.0;
            case "FLIGHT": return 800.0; // only for very long-distance estimations
            default: return 30.0;
        }
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
