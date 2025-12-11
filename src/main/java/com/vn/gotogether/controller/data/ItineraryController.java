package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.WarningDto;
import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryInvite;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.BlogService;
import com.vn.gotogether.service.data.ItineraryInviteService;
import com.vn.gotogether.service.data.ItineraryMediaService;
import com.vn.gotogether.service.data.ItineraryService;
import com.vn.gotogether.service.data.ItineraryValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final ItineraryItemRepository itemRepo;
    private final UserRepository userRepository;
    private final ItineraryInviteService inviteService;
    private final UserRepository userRepo;
    private final ItineraryValidationService itineraryValidationService;
    private final ItineraryMediaService mediaService;
    private final BlogService blogService;


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
//    @GetMapping
//    public List<ItinerarySummaryResponse> listMine() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
//
//        return itineraryService.listByUser(user.getId());
//    }

    @GetMapping
    public List<ItinerarySummaryResponse> listMine(
            @RequestParam(required = false) List<String> destinationIds,
            @RequestParam(defaultValue = "all") String type,  // owner, collaborator, all
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(defaultValue = "createdAt") String sortBy, // createdAt | startDate
            @RequestParam(defaultValue = "desc") String sortDir,     // asc | desc
            @RequestParam(defaultValue = "all") String period       // upcoming | ongoing | past | all
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        return itineraryService.listByUser(
                user.getId(),
                destinationIds,
                type,
                minDuration,
                maxDuration,
                sortBy,
                sortDir,
                period
        );
    }



    @GetMapping("/{id}/warnings")
    public ResponseEntity<?> getWarningsByDay(
            @PathVariable("id") String id,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String timezone) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth == null ? null : auth.getName();
        if (username == null) throw new com.vn.gotogether.exception.InvalidDataException("User không xác định");
        com.vn.gotogether.entity.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new com.vn.gotogether.exception.InvalidDataException("Người dùng không tồn tại."));

        // permission check: use ItineraryService helper
        if (!itineraryService.canViewItineraryForUser(id, user.getId())) {
            throw new com.vn.gotogether.exception.UnauthorizedException("Bạn không có quyền xem itinerary này");
        }

        String tz = (timezone == null || timezone.isBlank()) ? "Asia/Ho_Chi_Minh" : timezone;
        Map<Integer, List<WarningDto>> warningsByDay =
                itineraryValidationService.validateItineraryByDay(id, tz);

        if (day != null) {
            List<WarningDto> list = warningsByDay.getOrDefault(day, List.of());
            Map<String, Object> resp = Map.of(
                    "itineraryId", id,
                    "dayNumber", day,
                    "warnings", list
            );
            return ResponseEntity.ok(resp);
        } else {
            com.vn.gotogether.entity.Itinerary it = itineraryService.getItineraryEntity(id);
            Map<String, Object> resp = Map.of(
                    "itineraryId", id,
                    "startDate", it.getStartDate(),
                    "endDate", it.getEndDate(),
                    "warningsByDay", warningsByDay
            );
            return ResponseEntity.ok(resp);
        }
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

    @PatchMapping("/{id}/rename")
    public ResponseEntity<ItineraryResponse> renameItinerary(
            @PathVariable("id") String id,
            @RequestBody RenameItineraryRequest req
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        Itinerary updated = itineraryService.renameItinerary(user.getId(), id, req.getNewTitle());
        return ResponseEntity.ok(ItineraryResponse.builder()
                .id(updated.getId())
                .build());
    }
    @PatchMapping("/{id}/dates")
    public ResponseEntity<ItineraryResponse> updateDates(
            @PathVariable("id") String id,
            @RequestBody UpdateItineraryDatesRequest req
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        itineraryService.updateItineraryDates(user.getId(), id, req);
        return ResponseEntity.ok(ItineraryResponse.builder().id(id).build());
    }

    @PatchMapping("/{id}/public")
    public ResponseEntity<?> togglePublic(
            @PathVariable String id,
            @RequestParam boolean value
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        itineraryService.updatePublicStatus(id, user.getId(), value);
        return ResponseEntity.ok(Map.of("message", "Cập nhật chế độ công khai thành công"));
    }

    // GET: Lấy tất cả media
    @GetMapping("/{id}/media")
    public ResponseEntity<List<ItineraryMediaDto>> getAllMedia(
            @PathVariable("id") String itineraryId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(mediaService.getAllMedia(user.getId(), itineraryId));
    }

    // GET: Lấy media theo ngày
    @GetMapping("/{id}/media/day/{dayNumber}")
    public ResponseEntity<List<ItineraryMediaDto>> getMediaByDay(
            @PathVariable("id") String itineraryId,
            @PathVariable("dayNumber") Integer dayNumber,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(mediaService.getMediaByDay(user.getId(), itineraryId, dayNumber));
    }

    // POST: Upload media
    @PostMapping("/{id}/media")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ItineraryMediaDto> uploadMedia(
            @PathVariable("id") String itineraryId,
            @Valid @RequestBody UploadMediaRequest req,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(mediaService.uploadMedia(user.getId(), itineraryId, req));
    }

    // PUT: Cập nhật media
    @PutMapping("/{id}/media/{mediaId}")
    public ResponseEntity<ItineraryMediaDto> updateMedia(
            @PathVariable("id") String itineraryId,
            @PathVariable("mediaId") String mediaId,
            @Valid @RequestBody UpdateMediaRequest req,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(mediaService.updateMedia(user.getId(), itineraryId, mediaId, req));
    }

    // DELETE: Xóa media
    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable("id") String itineraryId,
            @PathVariable("mediaId") String mediaId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        mediaService.deleteMedia(user.getId(), itineraryId, mediaId);
        return ResponseEntity.noContent().build();
    }

    // GET: Thống kê media
    @GetMapping("/{id}/media/stats")
    public ResponseEntity<MediaStatsDto> getMediaStats(
            @PathVariable("id") String itineraryId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(mediaService.getMediaStats(user.getId(), itineraryId));
    }

    // Tạo blog từ itinerary
    @PostMapping("/{id}/blog")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BlogDetailResponse> createBlogFromItinerary(
            @PathVariable("id") String itineraryId,
            @Valid @RequestBody CreateBlogFromItineraryRequest req,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

        return ResponseEntity.ok(blogService.createBlogFromItinerary(user.getId(), itineraryId, req));
    }

    // Lấy danh sách blog từ itinerary
    @GetMapping("/{id}/blogs")
    public ResponseEntity<List<BlogSummaryResponse>> getBlogsByItinerary(
            @PathVariable("id") String itineraryId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại"));

//        // Kiểm tra quyền xem itinerary
//        if (!permissionService.canView(itineraryId, user.getId())) {
//            throw new AccessDeniedException("Bạn không có quyền xem blogs của lịch trình này");
//        }

        return ResponseEntity.ok(blogService.getBlogsByItinerary(itineraryId));
    }
}
