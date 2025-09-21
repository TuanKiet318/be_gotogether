package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.data.PlaceRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.PermissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryItemRepository itemRepo;
    private final PlaceRepository placeRepo;
    private final UserRepository userRepo;
    private final PermissionService permissionService;

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ItinerarySummaryResponse> listByUser(String userId) {
        var its = itineraryRepo.findByUser_IdOrderByCreatedAtDesc(userId);
        return its.stream().map(it ->
                ItinerarySummaryResponse.builder()
                        .id(it.getId())
                        .title(it.getTitle())
                        .startDate(it.getStartDate())
                        .endDate(it.getEndDate())
                        .totalItems(itemRepo.countByItinerary_Id(it.getId()))
                        .build()
        ).collect(Collectors.toList());
    }

    // ====== GET DETAIL: cho phép cả collaborator VIEW ======
    @Transactional(Transactional.TxType.SUPPORTS)
    public ItineraryDetailResponse getItineraryDetail(String userId, String itineraryId) {
        Itinerary it = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập lịch trình này");
        }

        var items = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);

        var itemDtos = items.stream().map(x ->
                ItineraryDetailResponse.Item.builder()
                        .id(x.getId())
                        .placeId(x.getPlace().getId())
                        .dayNumber(x.getDayNumber())
                        .orderInDay(x.getOrderInDay())
                        .startTime(x.getStartTime())
                        .endTime(x.getEndTime())
                        .description(x.getDescription())
                        .estimatedCost(x.getEstimatedCost())
                        .transportMode(x.getTransportMode() == null ? null : x.getTransportMode().name())
                        .build()
        ).collect(Collectors.toList());

        return ItineraryDetailResponse.builder()
                .id(it.getId())
                .title(it.getTitle())
                .startDate(it.getStartDate())
                .endDate(it.getEndDate())
                .items(itemDtos)
                .build();
    }

    // ====== CREATE ITINERARY ======
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

        Itinerary itin = Itinerary.builder()
                .user(user)
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

        return itin.getId();
    }

    // ================== Item CRUD / Import ==================

    // CREATE 1 item
    @Transactional
    public ItineraryItemDto createItem(String userId, String itineraryId, CreateItemRequest req) {
        ensureCanEdit(userId, itineraryId);
        Itinerary itin = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        int day = defaultDay(req.getDayNumber(), 1);
        validateDayInRange(itin, day);

        Place place = placeRepo.findById(req.getPlaceId())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy địa điểm: " + req.getPlaceId()));

        Integer order = req.getOrderInDay();
        if (order == null || order < 1) order = nextOrderInDay(itineraryId, day);

        LocalTime st = parseTimeStr(req.getStartTime());
        LocalTime en = parseTimeStr(req.getEndTime());
        if (st != null && en != null && !st.isBefore(en)) {
            throw new InvalidDataException("startTime phải trước endTime");
        }

        ItineraryItem item = ItineraryItem.builder()
                .itinerary(itin)
                .place(place)
                .dayNumber(day)
                .orderInDay(order)
                .startTime(st)
                .endTime(en)
                .description(req.getDescription())
                .estimatedCost(req.getEstimatedCost())
                .transportMode(parseModeStr(req.getTransportMode()))
                .build();

        itemRepo.save(item);

        return ItineraryItemDto.builder()
                .id(item.getId())
                .placeId(place.getId())
                .placeName(place.getName())
                .dayNumber(day)
                .orderInDay(order)
                .startTime(toStr(st))
                .endTime(toStr(en))
                .description(item.getDescription())
                .estimatedCost(item.getEstimatedCost())
                .transportMode(item.getTransportMode() == null ? null : item.getTransportMode().name())
                .build();
    }

    // UPDATE 1 item
    @Transactional
    public ItineraryItemDto updateItem(String userId, String itineraryId, String itemId, UpdateItemRequest req) {
        ensureCanEdit(userId, itineraryId);

        ItineraryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new InvalidDataException("Item không tồn tại"));
        if (!item.getItinerary().getId().equals(itineraryId))
            throw new InvalidDataException("Item không thuộc itinerary này");

        Integer oldDay = item.getDayNumber();

        if (req.getDayNumber() != null) {
            int newDay = req.getDayNumber();
            if (newDay < 1) throw new InvalidDataException("dayNumber >= 1");
            validateDayInRange(item.getItinerary(), newDay);
            item.setDayNumber(newDay);
        }

        if (req.getOrderInDay() != null && req.getOrderInDay() >= 1) {
            item.setOrderInDay(req.getOrderInDay());
        }

        if (req.getStartTime() != null) item.setStartTime(parseTimeStr(req.getStartTime()));
        if (req.getEndTime() != null) item.setEndTime(parseTimeStr(req.getEndTime()));
        if (item.getStartTime() != null && item.getEndTime() != null &&
                !item.getStartTime().isBefore(item.getEndTime())) {
            throw new InvalidDataException("startTime phải trước endTime");
        }

        if (req.getDescription() != null) item.setDescription(req.getDescription());
        if (req.getEstimatedCost() != null) item.setEstimatedCost(req.getEstimatedCost());
        if (req.getTransportMode() != null) item.setTransportMode(parseModeStr(req.getTransportMode()));

        itemRepo.save(item);

        // nếu chuyển sang ngày khác mà không set order thì đẩy xuống cuối
        if (!oldDay.equals(item.getDayNumber()) && req.getOrderInDay() == null) {
            int ord = nextOrderInDay(itineraryId, item.getDayNumber());
            item.setOrderInDay(ord);
            itemRepo.save(item);
        }

        Place place = item.getPlace();
        return ItineraryItemDto.builder()
                .id(item.getId())
                .placeId(place.getId())
                .placeName(place.getName())
                .dayNumber(item.getDayNumber())
                .orderInDay(item.getOrderInDay())
                .startTime(toStr(item.getStartTime()))
                .endTime(toStr(item.getEndTime()))
                .description(item.getDescription())
                .estimatedCost(item.getEstimatedCost())
                .transportMode(item.getTransportMode() == null ? null : item.getTransportMode().name())
                .build();
    }

    // DELETE 1 item
    @Transactional
    public void deleteItem(String userId, String itineraryId, String itemId) {
        ensureCanEdit(userId, itineraryId);
        ItineraryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new InvalidDataException("Item không tồn tại"));
        if (!item.getItinerary().getId().equals(itineraryId))
            throw new InvalidDataException("Item không thuộc itinerary này");

        itemRepo.delete(item);
    }

    @Transactional
    public void reorderInDay(String userId, String itineraryId, ReorderRequest req) {
        ensureCanEdit(userId, itineraryId);
        int day = req.dayNumber(); // <== đổi
        if (day < 1) throw new InvalidDataException("dayNumber >= 1");

        var items = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId)
                .stream().filter(i -> i.getDayNumber().equals(day))
                .collect(Collectors.toList());

        Map<String, ItineraryItem> byId = items.stream()
                .collect(Collectors.toMap(ItineraryItem::getId, x -> x));

        int order = 1;
        for (String id : req.itemIdsInOrder()) { // <== đổi
            ItineraryItem it = byId.get(id);
            if (it != null) it.setOrderInDay(order++);
        }
        itemRepo.saveAll(items);
    }

    // IMPORT nhiều địa điểm
    @Transactional
    public ImportPlacesResponse importPlaces(String userId, String itineraryId, ImportPlacesRequest req) {
        ensureCanEdit(userId, itineraryId);
        Itinerary itin = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        int defaultDay = defaultDay(req.getDefaultDay(), 1);
        boolean preventDup = Boolean.TRUE.equals(req.getPreventDuplicatesInDay());
        String mode = req.getAppendMode() == null ? "APPEND" : req.getAppendMode();

        int skipped = 0;
        List<ImportPlacesResponse.CreatedItem> created = new ArrayList<>();

        Map<Integer, Integer> maxOrderByDay = new HashMap<>();
        Map<Integer, Set<String>> existedByDay = new HashMap<>();

        if (preventDup) {
            var all = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);
            existedByDay = all.stream().collect(Collectors.groupingBy(
                    ItineraryItem::getDayNumber,
                    Collectors.mapping(i -> i.getPlace().getId(), Collectors.toSet())
            ));
        }

        for (ImportPlacesRequest.ImportItem it : req.getItems()) {
            int day = defaultDay(it.getDayNumber(), defaultDay);
            validateDayInRange(itin, day);

            if (preventDup && existedByDay.getOrDefault(day, Set.of()).contains(it.getPlaceId())) {
                skipped++; continue;
            }

            Place place = placeRepo.findById(it.getPlaceId())
                    .orElseThrow(() -> new InvalidDataException("Place không tồn tại: " + it.getPlaceId()));

            Integer order = it.getOrderInDay();
            if (order == null || order < 1) {
                int current = maxOrderByDay.computeIfAbsent(day, d -> nextOrderInDay(itineraryId, d) - 1);
                order = current + 1;
                maxOrderByDay.put(day, order);
            }

            LocalTime st = parseTimeStr(it.getStartTime());
            LocalTime en = parseTimeStr(it.getEndTime());
            if (st != null && en != null && !st.isBefore(en)) {
                throw new InvalidDataException("startTime phải trước endTime");
            }

            ItineraryItem newItem = ItineraryItem.builder()
                    .itinerary(itin)
                    .place(place)
                    .dayNumber(day)
                    .orderInDay(order)
                    .startTime(st)
                    .endTime(en)
                    .description(it.getDescription())
                    .estimatedCost(it.getEstimatedCost())
                    .transportMode(parseModeStr(it.getTransportMode()))
                    .build();

            itemRepo.save(newItem);

            if (preventDup) {
                existedByDay.computeIfAbsent(day, k -> new HashSet<>()).add(it.getPlaceId());
            }

            created.add(ImportPlacesResponse.CreatedItem.builder()
                    .id(newItem.getId())
                    .placeId(place.getId())
                    .dayNumber(day)
                    .orderInDay(order)
                    .startTime(toStr(st))
                    .endTime(toStr(en))
                    .build());
        }

        if ("REINDEX".equalsIgnoreCase(mode)) {
            var touchedDays = created.stream().map(ImportPlacesResponse.CreatedItem::getDayNumber)
                    .collect(Collectors.toSet());
            var all = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);
            for (Integer d : touchedDays) {
                var dayItems = all.stream().filter(i -> i.getDayNumber().equals(d))
                        .sorted(Comparator
                                .comparing((ItineraryItem i) -> i.getStartTime() == null)
                                .thenComparing(ItineraryItem::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(ItineraryItem::getOrderInDay))
                        .collect(Collectors.toList());
                int ord = 1;
                for (ItineraryItem i : dayItems) i.setOrderInDay(ord++);
                itemRepo.saveAll(dayItems);
            }
        }

        return ImportPlacesResponse.builder()
                .createdCount(created.size())
                .skippedDuplicates(skipped)
                .items(created)
                .build();
    }

    // LIST items (tuỳ chọn lọc dayNumber)
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ItineraryItemDto> listItems(String userId, String itineraryId, Integer dayNumber) {
        ensureCanView(userId, itineraryId);

        List<ItineraryItem> items = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);
        if (dayNumber != null) {
            items = items.stream().filter(i -> i.getDayNumber().equals(dayNumber)).collect(Collectors.toList());
        }
        return items.stream().map(i -> ItineraryItemDto.builder()
                .id(i.getId())
                .placeId(i.getPlace().getId())
                .placeName(i.getPlace().getName())
                .dayNumber(i.getDayNumber())
                .orderInDay(i.getOrderInDay())
                .startTime(toStr(i.getStartTime()))
                .endTime(toStr(i.getEndTime()))
                .description(i.getDescription())
                .estimatedCost(i.getEstimatedCost())
                .transportMode(i.getTransportMode()==null?null:i.getTransportMode().name())
                .build()
        ).collect(Collectors.toList());
    }

    // ===== Helpers dùng chung =====
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
    private Integer defaultDay(Integer v, int def) { return (v == null || v < 1) ? def : v; }
    private void validateDayInRange(Itinerary itin, int day) {
        LocalDate s = itin.getStartDate(), e = itin.getEndDate();
        if (s != null && e != null) {
            int max = (int)(e.toEpochDay() - s.toEpochDay()) + 1;
            if (day < 1 || day > max) throw new InvalidDataException("dayNumber ngoài phạm vi itinerary");
        }
    }
    private LocalTime parseTimeStr(String s) { return (s==null || s.isBlank()) ? null : LocalTime.parse(s); }
    private String toStr(LocalTime t) { return t==null ? null : t.toString(); }
    private ItineraryItem.TransportMode parseMode(String s) {
        if (s == null || s.isBlank()) return null;
        try { return ItineraryItem.TransportMode.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("transportMode không hợp lệ. Hợp lệ: WALK, BIKE, CAR, BUS, TRAIN, FLIGHT, BOAT");
        }
    }
    private ItineraryItem.TransportMode parseModeStr(String s) {
        if (s == null || s.isBlank()) return null;
        try { return ItineraryItem.TransportMode.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) {
            throw new InvalidDataException("transportMode không hợp lệ. Hợp lệ: WALK, BIKE, CAR, BUS, TRAIN, FLIGHT, BOAT");
        }
    }
}
