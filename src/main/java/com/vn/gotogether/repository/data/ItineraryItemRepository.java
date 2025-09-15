package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, String> {

    boolean existsByItinerary_IdAndDayNumberAndOrderInDay(String itineraryId, Integer dayNumber, Integer orderInDay);

    @Query("""
        select max(ii.orderInDay)
        from ItineraryItem ii
        where ii.itinerary.id = :itineraryId and ii.dayNumber = :dayNumber
    """)
    Integer findTopOrderInDayByItineraryAndDay(String itineraryId, Integer dayNumber);
}