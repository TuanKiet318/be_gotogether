package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.*;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryItemRepository itemRepo;
    private final PlaceRepository placeRepo;
    private final UserRepository userRepo;
    private final DestinationRepository destinationRepo;
    private final PermissionService permissionService;
    private final ItineraryInviteService inviteService;
    private final ItineraryInviteRepository inviteRepo;
    private final ItineraryCollaboratorRepository collaboratorRepo;
    private final ItineraryCollaboratorRepository itineraryCollaboratorRepository;

    // ItineraryService.java
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ItinerarySummaryResponse> listByUser(String userId) {

        // 1️⃣ Lấy danh sách lịch trình user là Owner
        var ownedIts = itineraryRepo.findByUser_IdOrderByCreatedAtDesc(userId);

        // 2️⃣ Lấy danh sách lịch trình user là Collaborator
        var collabIts = collaboratorRepo.findByUser_Id(userId).stream()
                .map(ItineraryCollaborator::getItinerary)
                .filter(Objects::nonNull)
                .toList();

        // 3️⃣ Gộp lại, loại trùng (tránh trường hợp vừa là owner vừa là collab)
        Map<String, Itinerary> merged = new LinkedHashMap<>();
        Stream.concat(ownedIts.stream(), collabIts.stream())
                .forEach(it -> merged.putIfAbsent(it.getId(), it));

        // 4️⃣ Trả về danh sách theo ItinerarySummaryResponse
        return merged.values().stream()
                .sorted(Comparator.comparing(Itinerary::getCreatedAt).reversed())
                .map(it -> ItinerarySummaryResponse.builder()
                        .id(it.getId())
                        .title(it.getTitle())
                        .startDate(it.getStartDate())
                        .endDate(it.getEndDate())
                        .totalItems(itemRepo.countByItinerary_Id(it.getId()))
                        .destinationId(it.getDestination() != null ? it.getDestination().getId() : null)
                        .destinationName(it.getDestination() != null ? it.getDestination().getName() : null)
                        .build()
                ).toList();
    }

    // src/main/java/com/vn/gotogether/service/data/ItineraryService.java
    @Transactional
    public String cloneItinerary(String userId, String sourceItineraryId, CloneItineraryRequest req) {
        // 1) Lấy itinerary nguồn + kiểm tra quyền xem
        Itinerary source = itineraryRepo.findById(sourceItineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình nguồn không tồn tại"));

//        if (!permissionService.canView(sourceItineraryId, userId)) {
//            throw new AccessDeniedException("Bạn không có quyền truy cập lịch trình nguồn");
//        }

        // 2) Lấy user + destination
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        Destination dest = source.getDestination();
        if (dest == null) {
            throw new InvalidDataException("Lịch trình nguồn không có Destination hợp lệ");
        }

        // 3) Xác định title & date range mới
        String newTitle = (req.getTitle() == null || req.getTitle().isBlank())
                ? source.getTitle() + " (Bản sao)"
                : req.getTitle().trim();

        LocalDate newStart = (req.getStartDate() == null) ? source.getStartDate() : req.getStartDate();
        LocalDate newEnd   = (req.getEndDate()   == null) ? source.getEndDate()   : req.getEndDate();

        if (newStart == null || newEnd == null || newStart.isAfter(newEnd)) {
            throw new InvalidDataException("Ngày bắt đầu/kết thúc không hợp lệ");
        }

        // 4) Tạo itinerary mới
        Itinerary cloned = Itinerary.builder()
                .user(user)
                .destination(dest)
                .title(newTitle)
                .startDate(newStart)
                .endDate(newEnd)
                .build();
        cloned = itineraryRepo.save(cloned);

        // 5) Copy items (nếu được yêu cầu)
        boolean includeItems = req.getIncludeItems() == null ? true : req.getIncludeItems();
        if (includeItems) {
            // số ngày mới
            long newDays = java.time.temporal.ChronoUnit.DAYS.between(newStart, newEnd) + 1;

            // Lấy items từ source (đã có repo sẵn)
            List<ItineraryItem> srcItems =
                    itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(sourceItineraryId);

            List<ItineraryItem> toSave = new ArrayList<>(srcItems.size());

            for (ItineraryItem src : srcItems) {
                Integer day = src.getDayNumber();
                if (day == null || day < 1) day = 1;

                if (day > newDays) {
                    // Nếu item vượt phạm vi ngày mới
                    if (Boolean.TRUE.equals(req.getTrimItemsExceedingNewRange())) {
                        // Bỏ qua
                        continue;
                    } else {
                        // Báo lỗi
                        throw new InvalidDataException(
                                "Item có dayNumber=" + day + " vượt quá số ngày của lịch trình mới (" + newDays + ")");
                    }
                }

                // Build item mới
                ItineraryItem clonedItem = ItineraryItem.builder()
                        .itinerary(cloned)
                        .place(src.getPlace())                // giữ reference Place
                        .dayNumber(day)
                        .orderInDay(src.getOrderInDay() == null ? 0 : src.getOrderInDay())
                        .startTime(src.getStartTime())
                        .endTime(src.getEndTime())
                        .description(src.getDescription())
                        .estimatedCost(src.getEstimatedCost())
                        .transportMode(src.getTransportMode())
                        .build();

                toSave.add(clonedItem);
            }

            if (!toSave.isEmpty()) {
                itemRepo.saveAll(toSave);
            }
        }

        // 6) Không copy invites/quyền; new owner = current user

        return cloned.getId();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public ItineraryDetailResponse getItineraryDetail(String userId, String itineraryId) {
        Itinerary it = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập lịch trình này");
        }

        // ==== TÍNH ROLE CỦA NGƯỜI DÙNG HIỆN TẠI TRONG ITINERARY ====
        String myRole = "VIEWER";
        boolean owner = it.getUser() != null && userId.equals(it.getUser().getId());
        if (owner) {
            myRole = "OWNER";
        } else {
            // nếu có dùng JPA repo: itineraryCollaboratorRepository.findByItineraryIdAndUserId(...)
            var collabOpt = itineraryCollaboratorRepository.findByItineraryIdAndUserId(itineraryId, userId);
            if (collabOpt.isPresent()) {
                myRole = collabOpt.get().getRole().name(); // EDITOR | VIEWER
            } else {
                // có thể là share-public (nếu app hỗ trợ), giữ VIEWER
                myRole = "VIEWER";
            }
        }

        boolean canEdit = permissionService.canEdit(itineraryId, userId);

        var items = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);

        var itemDtos = items.stream().map(x -> {
            var place = x.getPlace();

            String imageUrl = null;
            if (place.getImages() != null && !place.getImages().isEmpty()) {
                imageUrl = place.getImages().iterator().next().getImageUrl();
            }

            return ItineraryDetailResponse.Item.builder()
                    .id(x.getId())
                    .placeId(place.getId())
                    .placeName(place.getName())
                    .placeAddress(place.getAddress())
                    .placeImage(imageUrl)
                    .lat(place.getLat())
                    .lng(place.getLon())
                    .dayNumber(x.getDayNumber())
                    .orderInDay(x.getOrderInDay())
                    .startTime(x.getStartTime())
                    .endTime(x.getEndTime())
                    .description(x.getDescription())
                    .estimatedCost(x.getEstimatedCost())
                    .transportMode(x.getTransportMode() == null ? null : x.getTransportMode().name())
                    .build();
        }).toList();

        return ItineraryDetailResponse.builder()
                .id(it.getId())
                .title(it.getTitle())
                .startDate(it.getStartDate())
                .endDate(it.getEndDate())
                .destinationId(it.getDestination().getId())
                .destinationName(it.getDestination().getName())
                .items(itemDtos)
                .myRole(myRole)
                .canEdit(canEdit)
                .build();
    }

    // ===== CREATE ITINERARY =====
    @Transactional
    public String createItinerary(String userId, CreateItineraryRequest req) {
        if (req.getStartDate() == null || req.getEndDate() == null || req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu/kết thúc không hợp lệ");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        Destination destination = destinationRepo.findById(req.getDestinationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination không tồn tại: " + req.getDestinationId()));

        Itinerary itin = Itinerary.builder()
                .user(user)
                .destination(destination)
                .title(req.getTitle().trim())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();
        itin = itineraryRepo.save(itin);

        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            List<ItineraryItem> toSave = new ArrayList<>();

            for (CreateItineraryItemRequest i : req.getItems()) {
                if (i.getDayNumber() == null || i.getDayNumber() < 1 || i.getDayNumber() > days) {
                    throw new IllegalArgumentException("dayNumber không nằm trong phạm vi chuyến đi");
                }

                Place place = placeRepo.findById(i.getPlaceId())
                        .orElseThrow(() -> new IllegalArgumentException("Place không tồn tại: " + i.getPlaceId()));

                validateTimes(i.getStartTime(), i.getEndTime());

                int ord = (i.getOrderInDay() == null || i.getOrderInDay() <= 0)
                        ? nextOrderInDay(itin.getId(), i.getDayNumber())
                        : i.getOrderInDay();

                if (itemRepo.existsByItinerary_IdAndDayNumberAndOrderInDay(itin.getId(), i.getDayNumber(), ord)) {
                    ord = nextOrderInDay(itin.getId(), i.getDayNumber());
                }

                ItineraryItem item = ItineraryItem.builder()
                        .itinerary(itin)
                        .place(place)
                        .dayNumber(i.getDayNumber())
                        .orderInDay(ord)
                        .startTime(i.getStartTime())
                        .endTime(i.getEndTime())
                        .description(i.getDescription())
                        .estimatedCost(i.getEstimatedCost())
                        .transportMode(parseMode(i.getTransportMode()))
                        .build();

                toSave.add(item);
            }
            itemRepo.saveAll(toSave);
        }

        // === Invites ===
        final String itineraryId = itin.getId();
        final List<CreateItineraryRequest.InviteBody> invites = req.getInvites();
        final List<ItineraryInvite> savedInvites = new ArrayList<>();

        if (invites != null && !invites.isEmpty()) {
            for (CreateItineraryRequest.InviteBody ib : invites) {
                try {
                    String email = ib.getInviteEmail() == null ? null : ib.getInviteEmail().trim().toLowerCase();
                    if (email == null || email.isBlank()) continue;
                    if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) continue;

                    ItineraryInvite.Role role = (ib.getRole() == null)
                            ? ItineraryInvite.Role.EDITOR
                            : ItineraryInvite.Role.valueOf(ib.getRole().trim().toUpperCase());

                    ItineraryInvite invite = ItineraryInvite.builder()
                            .itinerary(itin)
                            .inviter(user)
                            .inviteEmail(email)
                            .inviteToken(UUID.randomUUID().toString().replace("-", ""))
                            .createdAt(Instant.now())
                            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                            .status(ItineraryInvite.Status.PENDING)
                            .role(role)
                            .build();

                    inviteRepo.save(invite);
                    savedInvites.add(invite);
                } catch (Exception ex) {
                    log.warn("Skipping invite for {} due to {}", ib.getInviteEmail(), ex.getMessage());
                }
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (ItineraryInvite inv : savedInvites) {
                        try {
                            inviteService.sendInviteEmailOnly(inv);
                        } catch (Exception ex) {
                            log.warn("Failed to send email to {}: {}", inv.getInviteEmail(), ex.getMessage());
                        }
                    }
                }
            });
        }

        return itineraryId;
    }

    // ===== FEATURED / ALL =====
    public List<ItinerarySummaryResponse> getFeaturedItineraries(String destinationId, int limit) {
        return itineraryRepo.findByDestination_IdAndIsFeaturedTrue(destinationId, PageRequest.of(0, limit))
                .stream()
                .map(i -> ItinerarySummaryResponse.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .startDate(i.getStartDate())
                        .endDate(i.getEndDate())
                        .totalItems(i.getItems().size())
                        .destinationId(i.getDestination().getId())
                        .destinationName(i.getDestination().getName())
                        .build())
                .toList();
    }

    public Page<ItinerarySummaryResponse> getAllItineraries(String destinationId, int page, int size) {
        return itineraryRepo.findByDestination_Id(destinationId, PageRequest.of(page, size))
                .map(i -> ItinerarySummaryResponse.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .startDate(i.getStartDate())
                        .endDate(i.getEndDate())
                        .totalItems(i.getItems().size())
                        .destinationId(i.getDestination().getId())
                        .destinationName(i.getDestination().getName())
                        .build());
    }


    // ===== Helpers =====
    private void ensureCanEdit(String userId, String itineraryId) {
        if (!permissionService.canEdit(itineraryId, userId))
            throw new AccessDeniedException("Bạn không có quyền sửa itinerary này");
    }
    private void ensureCanView(String userId, String itineraryId) {
        if (!permissionService.canView(itineraryId, userId))
            throw new AccessDeniedException("Bạn không có quyền truy cập itinerary này");
    }
    private void validateTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("endTime không được trước startTime");
        }
    }
    private int nextOrderInDay(String itineraryId, int dayNumber) {
        Integer last = itemRepo.findTopOrderInDayByItineraryAndDay(itineraryId, dayNumber);
        return (last == null ? 0 : last) + 1;
    }
    private ItineraryItem.TransportMode parseMode(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return ItineraryItem.TransportMode.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("transportMode không hợp lệ. Hợp lệ: WALK, BIKE, CAR, BUS, TRAIN, FLIGHT, BOAT");
        }
    }

    @Transactional
    public void deleteItineraryForUser(String itineraryId, String userEmail) {
        // Lấy itinerary
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new EntityNotFoundException("Itinerary not found"));

        // Lấy user hiện tại
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Kiểm tra quyền sở hữu
        if (!itinerary.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this itinerary");
        }

        // Xóa toàn bộ items trước (tránh lỗi constraint)
        itemRepo.deleteAllByItinerary_Id(itineraryId);


        // Xóa itinerary
        itineraryRepo.delete(itinerary);
    }

    @Transactional
    public Itinerary renameItinerary(String userId, String itineraryId, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new InvalidDataException("Tên lịch trình không được để trống");
        }

        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        // Chỉ cho phép chủ sở hữu sửa tên
        if (!itinerary.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền đổi tên lịch trình này");
        }

        itinerary.setTitle(newTitle.trim());
        return itineraryRepo.save(itinerary);
    }
    @Transactional
    public void updateItineraryDates(String userId, String itineraryId, UpdateItineraryDatesRequest req) {
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        if (!itinerary.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa lịch trình này");
        }

        LocalDate newStart = req.getStartDate();
        LocalDate newEnd = req.getEndDate();

        if (newStart == null || newEnd == null || newStart.isAfter(newEnd)) {
            throw new InvalidDataException("Ngày bắt đầu/kết thúc không hợp lệ");
        }

        LocalDate oldStart = itinerary.getStartDate();
        LocalDate oldEnd = itinerary.getEndDate();

        // Nếu rút ngắn thời gian (ví dụ từ 5 ngày còn 3)
        long oldDays = java.time.temporal.ChronoUnit.DAYS.between(oldStart, oldEnd) + 1;
        long newDays = java.time.temporal.ChronoUnit.DAYS.between(newStart, newEnd) + 1;

        if (newDays < oldDays) {
            // Xóa item có dayNumber > newDays
            var itemsToDelete = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId)
                    .stream()
                    .filter(i -> i.getDayNumber() != null && i.getDayNumber() > newDays)
                    .toList();

            if (!itemsToDelete.isEmpty()) {
                itemRepo.deleteAll(itemsToDelete);
            }
        }

        itinerary.setStartDate(newStart);
        itinerary.setEndDate(newEnd);
        itineraryRepo.save(itinerary);
    }

}
