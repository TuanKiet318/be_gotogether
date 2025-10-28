package com.vn.gotogether.repository.data;


import com.vn.gotogether.entity.TourParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TourParticipantRepository extends JpaRepository<TourParticipant, String> {
    List<TourParticipant> findByTourId(String tourId);
    boolean existsByTourIdAndUserId(String tourId, String userId);
    Optional<TourParticipant> findByTourIdAndUserId(String tourId, String userId);

}