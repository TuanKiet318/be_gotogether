// src/main/java/com/vn/gotogether/service/data/ItineraryTimeValidation.java
package com.vn.gotogether.service.data;

import com.vn.gotogether.entity.Itinerary;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@UtilityClass
public class ItineraryTimeValidation {

    public enum ItineraryTimeStatus {
        PAST,      // Đã qua
        ONGOING,   // Đang diễn ra
        UPCOMING   // Sắp tới
    }

    /**
     * Xác định trạng thái thời gian của itinerary
     */
    public static ItineraryTimeStatus getTimeStatus(Itinerary itinerary, String timezone) {
        if (itinerary.getStartDate() == null || itinerary.getEndDate() == null) {
            return ItineraryTimeStatus.UPCOMING; // Mặc định cho lịch trình chưa có ngày
        }

        ZoneId zone = ZoneId.of(timezone != null && !timezone.isBlank() ? timezone : "Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate today = now.toLocalDate();

        LocalDate startDate = itinerary.getStartDate();
        LocalDate endDate = itinerary.getEndDate();

        if (today.isAfter(endDate)) {
            return ItineraryTimeStatus.PAST;
        } else if (today.isBefore(startDate)) {
            return ItineraryTimeStatus.UPCOMING;
        } else {
            return ItineraryTimeStatus.ONGOING;
        }
    }

    /**
     * Kiểm tra xem itinerary có thể chỉnh sửa không
     * @throws ResponseStatusException nếu itinerary đã qua
     */
    public static void validateEditable(Itinerary itinerary, String timezone) {
        ItineraryTimeStatus status = getTimeStatus(itinerary, timezone);

        if (status == ItineraryTimeStatus.PAST) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Không thể chỉnh sửa lịch trình đã qua thời gian thực hiện"
            );
        }
    }

    /**
     * Kiểm tra xem itinerary có thể chỉnh sửa không (overload không cần timezone)
     */
    public static void validateEditable(Itinerary itinerary) {
        validateEditable(itinerary, "Asia/Ho_Chi_Minh");
    }
}