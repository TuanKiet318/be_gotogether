package com.vn.gotogether.controller.data;


import com.vn.gotogether.dto.data.CollaboratorResponseDto;
import com.vn.gotogether.dto.data.InviteActionRequestDto;
import com.vn.gotogether.dto.data.InviteRequestDto;
import com.vn.gotogether.dto.data.InviteResponseDto;
import com.vn.gotogether.entity.ItineraryCollaborator;
import com.vn.gotogether.entity.ItineraryInvite;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries/{itineraryId}")
@RequiredArgsConstructor
public class ItineraryInviteController {

    private final ItineraryInviteService inviteService;
    private final UserRepository userRepo;

    @PostMapping("/invites")
    public InviteResponseDto sendInvite(
            @PathVariable String itineraryId,
            @Valid @RequestBody InviteRequestDto dto) {

        dto.setItineraryId(itineraryId);
        String inviterId = currentUserIdOrThrow();
        return InviteResponseDto.fromEntity(inviteService.sendInvite(inviterId, dto));
    }

    // ===== Helpers =====
    private String currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new AccessDeniedException("Bạn cần đăng nhập.");
        }

        if (auth instanceof JwtAuthenticationToken jat) {
            String idClaim = jat.getToken().getClaimAsString("id");
            if (idClaim != null && !idClaim.isBlank()) {
                return userRepo.findById(idClaim)
                        .map(User::getId)
                        .orElseThrow(() -> new AccessDeniedException("Tài khoản không hợp lệ (id)."));
            }
        }

        String email = auth.getName();
        return userRepo.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không hợp lệ (email)."));
    }

//    // --- Accept / Decline ---
//    @PostMapping("/accept")
//    public InviteResponseDto acceptInvite(@RequestParam String token,
//                                          @RequestBody InviteActionRequestDto dto) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String userId = auth != null ? auth.getName() : null; // nếu chưa login → null
//        var invite = inviteService.handleInvite(token, userId, dto.getStatus());
//        return InviteResponseDto.fromEntity(invite);
//    }

    @PostMapping("/invites/decline")
    public String declineInvite(@RequestParam String token, @RequestParam String userId) {
        inviteService.declineInvite(token, userId);
        return "Invite declined";
    }

    @GetMapping("/invites")
    public List<InviteResponseDto> listInvites(@PathVariable String itineraryId) {
        return inviteService.listInvites(itineraryId).stream()
                .map(InviteResponseDto::fromEntity)
                .toList();
    }

    @GetMapping("/collaborators")
    public List<CollaboratorResponseDto> listCollaborators(@PathVariable String itineraryId) {
        return inviteService.listCollaborators(itineraryId).stream()
                .map(CollaboratorResponseDto::fromEntity)
                .toList();
    }

    @DeleteMapping("/collaborators/{userId}")
    public String removeCollaborator(@PathVariable String itineraryId, @PathVariable String userId) {
        inviteService.removeCollaborator(itineraryId, userId);
        return "Collaborator removed";
    }

}