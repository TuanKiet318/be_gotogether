package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.UpdateCollaboratorRoleRequest;
import com.vn.gotogether.service.data.ItineraryCollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryCollaboratorController {

    private final ItineraryCollaboratorService collaboratorService;

    /**
     * API: Cập nhật quyền (role) của collaborator trong một itinerary
     * Ví dụ:
     * PUT /api/itineraries/{itineraryId}/collaborators/{userId}/role
     * Body: { "role": "EDITOR" }
     */
    @PutMapping("/{itineraryId}/collaborators/{userId}/role")
    public ResponseEntity<String> updateCollaboratorRole(
            @PathVariable String itineraryId,
            @PathVariable String userId,
            @RequestBody UpdateCollaboratorRoleRequest request
    ) {
        collaboratorService.updateCollaboratorRole(itineraryId, userId, request.getRole());
        return ResponseEntity.ok("Cập nhật quyền truy cập thành công");
    }
}
