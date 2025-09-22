package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.InviteRequestDto;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.repository.data.ItineraryCollaboratorRepository;
import com.vn.gotogether.repository.data.ItineraryInviteRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.SimpleEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryInviteService {

    private final ItineraryInviteRepository inviteRepo;
    private final ItineraryRepository itineraryRepo;
    private final UserRepository userRepo;
    private final ItineraryCollaboratorRepository collaboratorRepo;
    private final SimpleEmailService emailService;

    @Transactional
    public ItineraryInvite sendInvite(String inviterId, InviteRequestDto dto) {
        var itinerary = itineraryRepo.findById(dto.getItineraryId())
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));

        var inviter = userRepo.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        // Check duplicate invite
        if (inviteRepo.existsByItineraryIdAndInviteEmail(dto.getItineraryId(), dto.getInviteEmail())) {
            throw new RuntimeException("Invite already sent to this email");
        }

        // Build invite
        ItineraryInvite invite = ItineraryInvite.builder()
                .itinerary(itinerary)
                .inviter(inviter)
                .inviteEmail(dto.getInviteEmail())
                .inviteToken(UUID.randomUUID().toString().replace("-", ""))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(ItineraryInvite.Status.PENDING)
                .role(dto.getRole() != null ? ItineraryInvite.Role.valueOf(dto.getRole()) : ItineraryInvite.Role.EDITOR)
                .build();

        inviteRepo.save(invite);
// Tên và email của người mời
        String inviterName = inviter.getName();
        String inviterEmail = inviter.getEmail();

// Role được mời
        String inviteRole = invite.getRole().name();

// Link chấp nhận lời mời
        String link = "http://your-frontend.com/invite?token=" + invite.getInviteToken();

// Nội dung email mới
        String subject = inviterName + " đã mời bạn tham gia lịch trình";
        String body = "<p>" + inviterName + " (" + inviterEmail + ") đã mời bạn tham gia lịch trình: <strong>"
                + itinerary.getTitle() + "</strong></p>"
                + "<p>Vai trò của bạn trong lịch trình: <strong>" + inviteRole + "</strong></p>"
                + "<p>Nhấn vào đây để chấp nhận lời mời: <a href=\"" + link + "\">Tham gia</a></p>"
                + "<p>Lời mời này sẽ hết hạn vào: " + invite.getExpiresAt() + "</p>";

        emailService.sendEmail(dto.getInviteEmail(), subject, body);

        return invite;
    }
    public Optional<ItineraryInvite> getInviteByToken(String token) {
        return inviteRepo.findByInviteToken(token)
                .filter(invite -> invite.getExpiresAt().isAfter(Instant.now()));
    }
    public String getUserIdFromInviteToken(String token) {
        Optional<ItineraryInvite> inviteOpt = inviteRepo.findByInviteToken(token);
        if (inviteOpt.isEmpty()) return null;

        ItineraryInvite invite = inviteOpt.get();
        // Nếu user đã tồn tại trong hệ thống
        Optional<User> userOpt = userRepo.findByEmail(invite.getInviteEmail());
        return userOpt.map(User::getId).orElse(null);
    }

    // --- Accept / Decline ---
    @Transactional
    public ItineraryInvite acceptOrDeclineInvite(String inviteToken, User currentUser, String action) {
        var invite = inviteRepo.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Invite token không tồn tại"));

        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(ItineraryInvite.Status.EXPIRED);
            inviteRepo.save(invite);
            throw new InvalidDataException("Invite đã hết hạn");
        }

        User user;
        if (currentUser != null) {
            user = currentUser;
            // Kiểm tra user có phải người được mời không
            if (!invite.getInviteEmail().equals(user.getEmail())) {
                throw new UnauthorizedException("User không được phép tham gia lịch trình này");
            }
        } else {
            // Chưa login
            throw new UnauthorizedException("Bạn cần đăng nhập để thực hiện hành động này");
        }

        if ("ACCEPTED".equalsIgnoreCase(action)) {
            invite.setStatus(ItineraryInvite.Status.ACCEPTED);

            // tạo collaborator nếu chưa tồn tại
            if (!collaboratorRepo.existsByItineraryIdAndUserId(invite.getItinerary().getId(), user.getId())) {
                ItineraryCollaborator.Role role = (invite.getRole() == ItineraryInvite.Role.EDITOR)
                        ? ItineraryCollaborator.Role.EDITOR
                        : ItineraryCollaborator.Role.VIEWER;

                ItineraryCollaborator collaborator = ItineraryCollaborator.builder()
                        .itinerary(invite.getItinerary())
                        .user(user)
                        .role(role)
                        .addedAt(Instant.now())
                        .build();
                collaboratorRepo.save(collaborator);
            }

        } else if ("DECLINED".equalsIgnoreCase(action)) {
            invite.setStatus(ItineraryInvite.Status.DECLINED);
        } else {
            throw new RuntimeException("Hành động không hợp lệ");
        }

        return inviteRepo.save(invite);
    }
    @Transactional
    public void acceptInvite(String token, String userId) {
        var invite = inviteRepo.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(ItineraryInvite.Status.EXPIRED);
            throw new RuntimeException("Invite expired");
        }

        var user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (collaboratorRepo.existsByItineraryIdAndUserId(invite.getItinerary().getId(), user.getId())) {
            throw new RuntimeException("Already a collaborator");
        }

        ItineraryCollaborator collaborator = ItineraryCollaborator.builder()
                .itinerary(invite.getItinerary())
                .user(user)
                .role(ItineraryCollaborator.Role.EDITOR)
                .build();

        collaboratorRepo.save(collaborator);

        invite.setStatus(ItineraryInvite.Status.ACCEPTED);
        inviteRepo.save(invite);
    }

    @Transactional
    public void declineInvite(String token, String userId) {
        var invite = inviteRepo.findByInviteToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (!invite.getInviteEmail().equalsIgnoreCase(userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getEmail())) {
            throw new RuntimeException("Not authorized to decline this invite");
        }

        invite.setStatus(ItineraryInvite.Status.DECLINED);
        inviteRepo.save(invite);
    }

    public List<ItineraryInvite> listInvites(String itineraryId) {
        return inviteRepo.findByItineraryId(itineraryId);
    }

    public List<ItineraryCollaborator> listCollaborators(String itineraryId) {
        return collaboratorRepo.findAll()
                .stream()
                .filter(c -> c.getItinerary().getId().equals(itineraryId))
                .toList();
    }

    @Transactional
    public void removeCollaborator(String itineraryId, String userId) {
        var collab = collaboratorRepo.findByItineraryIdAndUserId(itineraryId, userId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));
        collaboratorRepo.delete(collab);
    }
}