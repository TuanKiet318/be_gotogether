package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {
    List<Itinerary> findByUser_IdOrderByCreatedAtDesc(String userId);
}
