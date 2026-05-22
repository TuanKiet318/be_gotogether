package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.ApiResponse;
import com.vn.gotogether.service.data.ItineraryWeatherAlertScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather-alerts")
@RequiredArgsConstructor
public class WeatherAlertTestController {

    private final ItineraryWeatherAlertScheduler weatherAlertScheduler;

    /**
     * Trigger cảnh báo thời tiết thủ công (FOR TESTING ONLY)
     * POST /api/v1/weather-alerts/trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<String>> triggerWeatherAlert() {
        try {
            weatherAlertScheduler.notifyUpcomingItinerariesWeather();
            return ResponseEntity.ok(
                    ApiResponse.success("Weather alert triggered successfully", "Scheduler executed")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Error triggering weather alert: " + e.getMessage()));
        }
    }
}
