package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryInvite;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryInviteService;
import com.vn.gotogether.service.data.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final ItineraryItemRepository itemRepo;
    private final UserRepository userRepository;
    private final ItineraryInviteService inviteService;
    private final UserRepository userRepo;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryResponse create(
                                    @Valid @RequestBody CreateItineraryRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        String id = itineraryService.createItinerary(user.getId(), req);
        return ItineraryResponse.builder().id(id).build();
    }


    // src/main/java/com/vn/gotogether/controller/data/ItineraryController.java
    @PostMapping("/{sourceId}/clone")
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryResponse cloneItinerary(
            @PathVariable("sourceId") String sourceId,
            @Valid @RequestBody CloneItineraryRequest req
    ) {
        // Lấy user hiện tại
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        String newId = itineraryService.cloneItinerary(user.getId(), sourceId, req);
        return ItineraryResponse.builder().id(newId).build();
    }

    // ====== GET LIST: các lịch trình của tôi ======
    @GetMapping
    public List<ItinerarySummaryResponse> listMine() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        return itineraryService.listByUser(user.getId());
    }


    // ====== GET DETAIL: chi tiết 1 lịch trình (kèm items) ======
    @GetMapping("/{id}")
    public ItineraryDetailResponse getOne(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        return itineraryService.getItineraryDetail(user.getId(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItinerary(@PathVariable String id, Authentication authentication) {
        itineraryService.deleteItineraryForUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invites/{token}")
    public InviteResponseDto getInviteByToken(@PathVariable String token) {
            var invite = inviteService.getInviteByToken(token)
                .orElseThrow(() -> new RuntimeException("Invite not found or expired"));
        return InviteResponseDto.fromEntity(invite);
    }
    @PostMapping("/invites/accept")
    public InviteResponseDto acceptInvite(
            @RequestParam String token,
            @RequestBody InviteActionRequestDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {

            currentUser = userRepo.findByEmail(auth.getName())
                    .orElseThrow(() -> new UnauthorizedException("User không tồn tại"));

        }

        try {
            // Chuyển currentUser = null nếu chưa login
            ItineraryInvite updatedInvite = inviteService.acceptOrDeclineInvite(token, currentUser, dto.getStatus());
            return InviteResponseDto.fromEntity(updatedInvite);
        } catch (UnauthorizedException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (InvalidDataException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/by-destination/{destinationId}/featured")
    public List<ItinerarySummaryResponse> getFeaturedItineraries(@PathVariable String destinationId) {
        return itineraryService.getFeaturedItineraries(destinationId, 5);
    }

    @GetMapping("/by-destination/{destinationId}")
    public Page<ItinerarySummaryResponse> getAllItineraries(
            @PathVariable String destinationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return itineraryService.getAllItineraries(destinationId, page, size);
    }


}
