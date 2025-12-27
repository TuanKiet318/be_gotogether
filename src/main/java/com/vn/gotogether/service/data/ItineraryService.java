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
    private final TagRepository tagRepo;

    @Transactional
    public ItineraryFeaturedDetailResponse featureItinerary(
            String itineraryId,
            FeatureItineraryRequest request,
            Set<String> heroImageUrls
    ) {
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        Set<Tag> tags = tagRepo.findAllById(request.getTagIds())
                .stream()
                .collect(Collectors.toSet());

        itinerary.setOverview(request.getOverview());
        itinerary.setImageHero(heroImageUrls);
        itinerary.setTags(tags);
        itinerary.setFeatured(true);
        itinerary.setPublic(true);

        return mapToFeaturedDetail(itinerary);
    }

    private ItineraryFeaturedDetailResponse mapToFeaturedDetail(Itinerary itinerary) {
        return ItineraryFeaturedDetailResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .overview(itinerary.getOverview())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .destinationId(itinerary.getDestination().getId())
                .destinationName(itinerary.getDestination().getName())
                .heroImages(itinerary.getImageHero())
                .tags(
                        itinerary.getTags().stream()
                                .map(tag -> ItineraryFeaturedDetailResponse.TagResponse.builder()
                                        .id(tag.getId())
                                        .name(tag.getName())
                                        .slug(tag.getCode())
                                        .build()
                                )
                                .collect(Collectors.toSet())
                )
                .items(List.of()) // mới tạo chưa có item
                .build();
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ItinerarySummaryResponse> listByUser(
            String userId,
            List<String> destinationIds,
            String type,
            Integer minDuration,
            Integer maxDuration,
            String sortBy,
            String sortDir,
            String period // new param
    ) {
        List<Itinerary> ownedIts = itineraryRepo.findByUser_IdOrderByCreatedAtDesc(userId);
        List<Itinerary> collabIts = collaboratorRepo.findByUser_Id(userId).stream()
                .map(ItineraryCollaborator::getItinerary)
                .filter(Objects::nonNull)
                .toList();

        Stream<Itinerary> stream;
        switch (type.toLowerCase()) {
            case "owner" -> stream = ownedIts.stream();
            case "collaborator" -> stream = collabIts.stream();
            default -> stream = Stream.concat(ownedIts.stream(), collabIts.stream());
        }

        // destination filter
        if (destinationIds != null && !destinationIds.isEmpty()) {
            stream = stream.filter(it ->
                    it.getDestination() != null &&
                            destinationIds.contains(it.getDestination().getId())
            );
        }

        // duration filter (days)
        stream = stream.filter(it -> {
            if (it.getStartDate() == null || it.getEndDate() == null) return true;
            long duration = ChronoUnit.DAYS.between(it.getStartDate(), it.getEndDate()) + 1;
            boolean ok = true;
            if (minDuration != null) ok &= duration >= minDuration;
            if (maxDuration != null) ok &= duration <= maxDuration;
            return ok;
        });

        // period filter: upcoming, ongoing, past, all
        if (period != null && !period.equalsIgnoreCase("all")) {
            LocalDate today = LocalDate.now(); // nếu start/end là LocalDate
            String p = period.toLowerCase();
            stream = stream.filter(it -> {
                LocalDate s = it.getStartDate();
                LocalDate e = it.getEndDate();
                if (s == null || e == null) return false;

                return switch (p) {
                    case "upcoming" -> s.isAfter(today);
                    case "ongoing" -> ( !s.isAfter(today) && !e.isBefore(today) ); // s <= today && e >= today
                    case "past" -> e.isBefore(today);
                    default -> true;
                };
            });
        }

        // sorting
        Comparator<Itinerary> comparator = switch (sortBy) {
            case "startDate" -> Comparator.comparing(Itinerary::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Itinerary::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        if ("desc".equalsIgnoreCase(sortDir)) comparator = comparator.reversed();

        List<Itinerary> result = stream
                .sorted(comparator)
                .distinct()
                .toList();

        // map -> response
        return result.stream()
                .map(it -> ItinerarySummaryResponse.builder()
                        .id(it.getId())
                        .title(it.getTitle())
                        .startDate(it.getStartDate())
                        .endDate(it.getEndDate())
                        .totalItems(itemRepo.countByItinerary_Id(it.getId()))
                        .destinationId(it.getDestination() != null ? it.getDestination().getId() : null)
                        .destinationName(it.getDestination() != null ? it.getDestination().getName() : null)
                        .ownerId(it.getUser() != null ? it.getUser().getId() : null)
                        .ownerName(it.getUser() != null ? it.getUser().getName() : null)
                        .ownerAvatar(it.getUser() != null ? it.getUser().getAvatar() : null)
                        .isOwner(userId.equals(it.getUser() != null ? it.getUser().getId() : null))
                        .build())
                .toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ItinerarySummaryResponse> listByUser(
            String userId,
            List<String> destinationIds,
            String type,
            Integer minDuration,
            Integer maxDuration,
            String sortBy,
            String sortDir
    ) {
        List<Itinerary> ownedIts = itineraryRepo.findByUser_IdOrderByCreatedAtDesc(userId);
        List<Itinerary> collabIts = collaboratorRepo.findByUser_Id(userId).stream()
                .map(ItineraryCollaborator::getItinerary)
                .filter(Objects::nonNull)
                .toList();

        /** -----------------------
         * 1️⃣ Chọn loại (owner/collab/all)
         * ------------------------ */
        Stream<Itinerary> stream;

        switch (type.toLowerCase()) {
            case "owner" ->
                    stream = ownedIts.stream();
            case "collaborator" ->
                    stream = collabIts.stream();
            default ->  // all
                    stream = Stream.concat(ownedIts.stream(), collabIts.stream());
        }

        /** -----------------------
         * 2️⃣ Lọc theo destinationIds
         * ------------------------ */
        if (destinationIds != null && !destinationIds.isEmpty()) {
            stream = stream.filter(it ->
                    it.getDestination() != null &&
                            destinationIds.contains(it.getDestination().getId())
            );
        }

        /** -----------------------
         * 3️⃣ Lọc theo duration (ngày)
         * ------------------------ */
        stream = stream.filter(it -> {
            if (it.getStartDate() == null || it.getEndDate() == null) return true;

            long duration = ChronoUnit.DAYS.between(it.getStartDate(), it.getEndDate()) + 1;

            boolean ok = true;
            if (minDuration != null) ok &= duration >= minDuration;
            if (maxDuration != null) ok &= duration <= maxDuration;

            return ok;
        });

        /** -----------------------
         * 4️⃣ Sắp xếp theo sortBy + sortDir
         * ------------------------ */
        Comparator<Itinerary> comparator;

        comparator = switch (sortBy) {
            case "startDate" -> Comparator.comparing(Itinerary::getStartDate,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Itinerary::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if (sortDir.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        List<Itinerary> result = stream
                .sorted(comparator)
                .distinct()
                .toList();

        /** -----------------------
         * 5️⃣ Map thành Response
         * ------------------------ */
        return result.stream()
                .map(it -> ItinerarySummaryResponse.builder()
                        .id(it.getId())
                        .title(it.getTitle())
                        .startDate(it.getStartDate())
                        .endDate(it.getEndDate())
                        .totalItems(itemRepo.countByItinerary_Id(it.getId()))
                        .destinationId(it.getDestination() != null ? it.getDestination().getId() : null)
                        .destinationName(it.getDestination() != null ? it.getDestination().getName() : null)
                        .ownerId(it.getUser() != null ? it.getUser().getId() : null)
                        .ownerName(it.getUser() != null ? it.getUser().getName() : null)
                        .ownerAvatar(it.getUser() != null ? it.getUser().getAvatar() : null)
                        .isOwner(userId.equals(
                                it.getUser() != null ? it.getUser().getId() : null
                        ))
                        .build())
                .toList();
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

//        if (!it.isPublic()&&!permissionService.canView(itineraryId, userId)) {
//            throw new AccessDeniedException("Bạn không có quyền truy cập lịch trình này");
//        }

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

    public ItineraryFeaturedDetailResponse getFeaturedDetail(String itineraryId) {

        Itinerary itinerary = itineraryRepo
                .findFeaturedDetailById(itineraryId)
                .orElseThrow(() -> new RuntimeException("Featured itinerary not found"));

        return ItineraryFeaturedDetailResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .overview(itinerary.getOverview())
                .heroImages(itinerary.getImageHero())
                .destinationId(itinerary.getDestination().getId())
                .destinationName(itinerary.getDestination().getName())
                .tags(
                        itinerary.getTags().stream()
                                .map(tag -> ItineraryFeaturedDetailResponse.TagResponse.builder()
                                        .id(tag.getId())
                                        .name(tag.getName())
                                        .slug(tag.getCode())
                                        .build()
                                )
                                .collect(Collectors.toSet())
                )
                .items(
                        itinerary.getItems().stream()
                                .map(item -> ItineraryFeaturedDetailResponse.Item.builder()
                                        .id(item.getId())
                                        .placeId(item.getPlace().getId())
                                        .placeName(item.getPlace().getName())
                                        .placeAddress(item.getPlace().getAddress())
                                        .placeImage(item.getPlace().getImages().iterator().next().getImageUrl() )
                                        .lat(item.getPlace().getLat())
                                        .lng(item.getPlace().getLon())
                                        .dayNumber(item.getDayNumber())
                                        .orderInDay(item.getOrderInDay())
                                        .startTime(item.getStartTime())
                                        .endTime(item.getEndTime())
                                        .description(item.getDescription())
                                        .estimatedCost(item.getEstimatedCost())
                                        .transportMode(
                                                item.getTransportMode() != null
                                                        ? item.getTransportMode().name()
                                                        : null
                                        )
                                        .build()
                                )
                                .toList()
                )
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

    public List<ItineraryFeaturedResponse> getFeaturedItineraries(
            String destinationId,
            int limit
    ) {
        return itineraryRepo
                .findByDestination_IdAndIsFeaturedTrueAndIsPublicTrue(
                        destinationId,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(itinerary -> {


                    return ItineraryFeaturedResponse.builder()
                            .id(itinerary.getId())
                            .title(itinerary.getTitle())
                            .overview(itinerary.getOverview())

                            .startDate(itinerary.getStartDate())
                            .endDate(itinerary.getEndDate())
                            .totalDays(
                                    (int) (itinerary.getEndDate().toEpochDay()
                                            - itinerary.getStartDate().toEpochDay() + 1)
                            )
                            .totalItems(itinerary.getItems().size())

                            .destinationId(itinerary.getDestination().getId())
                            .destinationName(itinerary.getDestination().getName())

                            .heroImage(
                                    itinerary.getImageHero().stream().findFirst().orElse(null)
                            )

                            .tags(
                                    itinerary.getTags()
                                            .stream()
                                            .map(Tag::getName) // tiếng Việt
                                            .collect(Collectors.toSet())
                            )
                            .build();
                })
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

    @Transactional
    public void updatePublicStatus(String itineraryId, String userId, boolean value) {
        Itinerary it = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy lịch trình"));

        // Kiểm tra quyền: chỉ người tạo mới có thể bật/tắt public
        if (it.getUser() == null || !it.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền thay đổi chế độ công khai của lịch trình này");
        }

        it.setPublic(value);
        itineraryRepo.save(it);
    }
    public boolean canViewItineraryForUser(String itineraryId, String userId) {
        try {
            return permissionService.canView(itineraryId, userId);
        } catch (Exception ex) {
            return false;
        }
    }

    public Itinerary getItineraryEntity(String itineraryId) {
        return itineraryRepo.findById(itineraryId).orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));
    }
}
