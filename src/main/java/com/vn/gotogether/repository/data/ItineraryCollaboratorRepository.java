package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.ItineraryCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItineraryCollaboratorRepository extends JpaRepository<ItineraryCollaborator, String> {
    Optional<ItineraryCollaborator> findByItineraryIdAndUserId(String itineraryId, String userId);
    List<ItineraryCollaborator> findByItineraryId(String itineraryId);
    void deleteByItineraryIdAndUserId(String itineraryId, String userId);

    // Thêm 2 exists() để PermissionService dùng nhanh, không cần load entity
    boolean existsByItineraryIdAndUserId(String itineraryId, String userId);
    boolean existsByItineraryIdAndUserIdAndRole(String itineraryId, String userId, ItineraryCollaborator.Role role);

    // 🆕 Thêm hàm này để lấy tất cả itinerary mà user đang là collaborator
    List<ItineraryCollaborator> findByUser_Id(String userId);
    Optional<ItineraryCollaborator> findByItinerary_IdAndUser_Id(String itineraryId, String userId);
}
