package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItineraryInviteRepository extends JpaRepository<ItineraryInvite, String> {
    Optional<ItineraryInvite> findByInviteToken(String token);
    List<ItineraryInvite> findByInviteEmailAndStatus(String email, ItineraryInvite.Status status);
    List<ItineraryInvite> findByItineraryId(String itineraryId);
}