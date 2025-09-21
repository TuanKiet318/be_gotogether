package com.vn.gotogether.service.data;

import com.vn.gotogether.repository.data.ItineraryCollaboratorRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.entity.ItineraryCollaborator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryCollaboratorRepository collabRepo;

    public boolean isOwner(String itineraryId, String userId) {
        return itineraryRepo.findOwnerIdByItineraryId(itineraryId)
                .map(ownerId -> ownerId.equals(userId))
                .orElse(false);
    }

    public boolean canEdit(String itineraryId, String userId) {
        if (isOwner(itineraryId, userId)) return true;
        return collabRepo.existsByItineraryIdAndUserIdAndRole(
                itineraryId, userId, ItineraryCollaborator.Role.EDITOR);
    }

    public boolean canView(String itineraryId, String userId) {
        return canEdit(itineraryId, userId)
                || collabRepo.existsByItineraryIdAndUserId(itineraryId, userId);
    }
}
