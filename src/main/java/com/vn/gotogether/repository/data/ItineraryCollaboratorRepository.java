package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItineraryCollaboratorRepository extends JpaRepository<ItineraryCollaborator, String> {
    Optional<ItineraryCollaborator> findByItineraryIdAndUserId(String itineraryId, String userId);
    List<ItineraryCollaborator> findByItineraryId(String itineraryId);
    void deleteByItineraryIdAndUserId(String itineraryId, String userId);
}