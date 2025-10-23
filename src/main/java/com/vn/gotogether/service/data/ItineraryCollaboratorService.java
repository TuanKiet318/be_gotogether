package com.vn.gotogether.service.data;

import com.vn.gotogether.entity.ItineraryCollaborator;
import com.vn.gotogether.exception.InvalidDataException;

import com.vn.gotogether.repository.data.ItineraryCollaboratorRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItineraryCollaboratorService {

    private final ItineraryCollaboratorRepository collaboratorRepo;
    private final ItineraryRepository itineraryRepo;

    @Transactional
    public void updateCollaboratorRole(String itineraryId, String userId, String role) {
        var itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        var collab = collaboratorRepo.findByItinerary_IdAndUser_Id(itineraryId, userId)
                .orElseThrow(() -> new InvalidDataException("Người dùng không phải là collaborator"));

        if (!role.equalsIgnoreCase("EDITOR") && !role.equalsIgnoreCase("VIEWER")) {
            throw new InvalidDataException("Role không hợp lệ (chỉ cho phép VIEWER hoặc EDITOR)");
        }

        collab.setRole(ItineraryCollaborator.Role.valueOf(role.toUpperCase()));
        collaboratorRepo.save(collab);
    }
}