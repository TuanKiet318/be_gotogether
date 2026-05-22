package com.vn.gotogether.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastService {

    private final ObjectMapper objectMapper;

    @Value("${weather.api.base-url:https://api.open-meteo.com/v1/forecast}")
    private String weatherApiBaseUrl;

    @Value("${weather.api.timezone:Asia/Ho_Chi_Minh}")
    private String weatherApiTimezone;

    public Optional<WeatherAlertSnapshot> getDailyWeatherAlert(Double lat, Double lon, LocalDate targetDate) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(weatherApiBaseUrl)
                    .queryParam("latitude", lat)
                    .queryParam("longitude", lon)
                    .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum")
                    .queryParam("timezone", weatherApiTimezone)
                    .queryParam("start_date", targetDate)
                    .queryParam("end_date", targetDate)
                    .toUriString();

            String payload = RestClient.create()
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (payload == null || payload.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(payload);
            JsonNode daily = root.path("daily");
            JsonNode dates = daily.path("time");

            if (!dates.isArray() || dates.isEmpty()) {
                return Optional.empty();
            }

            int idx = 0;
            for (int i = 0; i < dates.size(); i++) {
                if (targetDate.toString().equals(dates.get(i).asText())) {
                    idx = i;
                    break;
                }
            }

            int weatherCode = daily.path("weather_code").path(idx).asInt(0);
            double tempMax = daily.path("temperature_2m_max").path(idx).asDouble(Double.NaN);
            double tempMin = daily.path("temperature_2m_min").path(idx).asDouble(Double.NaN);
            int rainChance = daily.path("precipitation_probability_max").path(idx).asInt(0);
            double rainMm = daily.path("precipitation_sum").path(idx).asDouble(0.0);

            String weatherText = mapWeatherCode(weatherCode);
            boolean severeByCode = weatherCode >= 61 || weatherCode == 95 || weatherCode == 96 || weatherCode == 99;
            boolean likelyRain = rainChance >= 50 || rainMm >= 10.0;
            boolean isAlert = severeByCode || likelyRain;

            String summary = String.format(
                    "%s. Nhiệt độ %.1f-%.1f°C, khả năng mưa %d%%, lượng mưa %.1f mm.",
                    weatherText,
                    tempMin,
                    tempMax,
                    rainChance,
                    rainMm
            );

            return Optional.of(new WeatherAlertSnapshot(isAlert, summary));
        } catch (Exception e) {
            log.warn("Failed to fetch weather forecast lat={}, lon={}, date={}", lat, lon, targetDate, e);
            return Optional.empty();
        }
    }

    private String mapWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Trời quang";
            case 1, 2, 3 -> "Có mây";
            case 45, 48 -> "Sương mù";
            case 51, 53, 55, 56, 57 -> "Mưa phùn";
            case 61, 63, 65, 66, 67 -> "Mưa";
            case 71, 73, 75, 77 -> "Tuyết";
            case 80, 81, 82 -> "Mưa rào";
            case 85, 86 -> "Mưa tuyết";
            case 95, 96, 99 -> "Dông";
            default -> "Thời tiết có biến động";
        };
    }

    public record WeatherAlertSnapshot(boolean alertWorthy, String summary) {
    }
}
