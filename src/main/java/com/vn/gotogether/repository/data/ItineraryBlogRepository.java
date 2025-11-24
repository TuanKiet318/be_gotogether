package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryBlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryBlogRepository extends JpaRepository<ItineraryBlog, String> {

    List<ItineraryBlog> findByItinerary_Id(String itineraryId);

    Optional<ItineraryBlog> findByItinerary_IdAndBlog_Id(String itineraryId, String blogId);

    boolean existsByItinerary_IdAndBlog_Id(String itineraryId, String blogId);

    void deleteByItinerary_Id(String itineraryId);
}