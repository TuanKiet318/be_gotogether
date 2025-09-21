package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, String> {

    long countByItinerary_Id(String itineraryId);

    List<ItineraryItem> findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(String itineraryId);

    // nếu bạn đã có 2 method dưới thì giữ nguyên:
    boolean existsByItinerary_IdAndDayNumberAndOrderInDay(String itineraryId, Integer dayNumber, Integer orderInDay);

    @org.springframework.data.jpa.repository.Query("""
        select max(ii.orderInDay)
        from ItineraryItem ii
        where ii.itinerary.id = :itineraryId and ii.dayNumber = :dayNumber
    """)
    Integer findTopOrderInDayByItineraryAndDay(String itineraryId, Integer dayNumber);
}
