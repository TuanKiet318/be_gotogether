package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryMediaRepository extends JpaRepository<ItineraryMedia, String> {

    List<ItineraryMedia> findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(String itineraryId);

    List<ItineraryMedia> findByItinerary_IdAndDayNumberOrderByOrderInDayAsc(String itineraryId, Integer dayNumber);

    @Query("SELECT COALESCE(MAX(m.orderInDay), 0) FROM ItineraryMedia m WHERE m.itinerary.id = :itineraryId AND m.dayNumber = :dayNumber")
    Integer findMaxOrderInDay(String itineraryId, Integer dayNumber);

    long countByItinerary_Id(String itineraryId);

    long countByItinerary_IdAndMediaType(String itineraryId, ItineraryMedia.MediaType mediaType);

    void deleteAllByItinerary_Id(String itineraryId);
}