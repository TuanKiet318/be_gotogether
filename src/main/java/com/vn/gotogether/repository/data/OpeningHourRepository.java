package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.OpeningHour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface OpeningHourRepository extends JpaRepository<OpeningHour, String> {
    List<OpeningHour> findByPlaceIdAndDate(String placeId, LocalDate date);
    List<OpeningHour> findByPlaceIdAndWeekday(String placeId, Integer weekday);
}
