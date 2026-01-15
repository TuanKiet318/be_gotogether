    package com.vn.gotogether.service.data;

    import com.vn.gotogether.dto.data.*;
    import com.vn.gotogether.entity.Itinerary;
    import com.vn.gotogether.entity.ItineraryItem;
    import com.vn.gotogether.entity.Place;
    import com.vn.gotogether.exception.InvalidDataException;
    import com.vn.gotogether.repository.data.ItineraryItemRepository;
    import com.vn.gotogether.repository.data.ItineraryRepository;
    import com.vn.gotogether.repository.data.PlaceRepository;
    import com.vn.gotogether.service.data.ActivityLogService;
    import jakarta.transaction.Transactional;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class ItineraryItemService {

        private final ItineraryRepository itineraryRepo;
        private final PlaceRepository placeRepo;
        private final ItineraryItemRepository itemRepo;
        private final PermissionService permissionService;
        private final ItineraryValidationService itineraryValidationService;
        // Tuỳ chọn: nếu có ActivityLogService riêng thì autowire, không có thì để null hoặc xoá hết phần log(...)
        // @Autowired(required = false)
        private ActivityLogService logService;

        // ===== CREATE =====
        @Transactional
        public ItineraryItemDto createItem(String userId, String itineraryId, CreateItemRequest req) {
            itineraryValidationService.validateItineraryEditable(itineraryId, userId);
            ensureCanEdit(userId, itineraryId);
            Itinerary itin = getItin(itineraryId);
            Integer day = defaultDay(req.getDayNumber(), 1);
            validateDayInRange(itin, day);

            Place place = placeRepo.findById(req.getPlaceId())
                    .orElseThrow(() -> new InvalidDataException("Không tìm thấy địa điểm: " + req.getPlaceId()));

            Integer orderInDay = req.getOrderInDay();
            if (orderInDay == null || orderInDay < 1) {
                orderInDay = itemRepo.findMaxOrderInDayOrZero(itineraryId, day) + 1; // <-- KHỚP REPO
            }

            LocalTime st = parseTime(req.getStartTime());
            LocalTime en = parseTime(req.getEndTime());
            if (st != null && en != null && !st.isBefore(en)) {
                throw new InvalidDataException("startTime phải trước endTime");
            }

            ItineraryItem newItem = ItineraryItem.builder()
                    .itinerary(itin)
                    .place(place)
                    .dayNumber(day)
                    .orderInDay(orderInDay)
                    .startTime(st)
                    .endTime(en)
                    .description(req.getDescription())
                    .estimatedCost(req.getEstimatedCost())
                    .transportMode(parseTransport(req.getTransportMode()))
                    .build();

            itemRepo.save(newItem);
            log(itineraryId, userId, "ITIN_ITEM_CREATED", Map.of("itemId", newItem.getId()));
            return toDto(newItem, place.getName());
        }

        // ===== UPDATE =====
        @Transactional
        public ItineraryItemDto updateItem(String userId, String itineraryId, String itemId, UpdateItemRequest req) {
            itineraryValidationService.validateItineraryEditable(itineraryId, userId);
            ensureCanEdit(userId, itineraryId);

            ItineraryItem item = itemRepo.findById(itemId)
                    .orElseThrow(() -> new InvalidDataException("Item không tồn tại"));
            if (!item.getItinerary().getId().equals(itineraryId)) {
                throw new InvalidDataException("Item không thuộc itinerary này");
            }

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

            if (req.getStartTime() != null) item.setStartTime(parseTime(req.getStartTime()));
            if (req.getEndTime() != null) item.setEndTime(parseTime(req.getEndTime()));
            if (item.getStartTime() != null && item.getEndTime() != null &&
                    !item.getStartTime().isBefore(item.getEndTime())) {
                throw new InvalidDataException("startTime phải trước endTime");
            }

            if (req.getDescription() != null) item.setDescription(req.getDescription());
            if (req.getEstimatedCost() != null) item.setEstimatedCost(req.getEstimatedCost());
            if (req.getTransportMode() != null) item.setTransportMode(parseTransport(req.getTransportMode()));

            itemRepo.save(item);

            if (!Objects.equals(oldDay, item.getDayNumber()) && req.getOrderInDay() == null) {
                int maxOrder = itemRepo.findMaxOrderInDayOrZero(itineraryId, item.getDayNumber()); // <-- KHỚP REPO
                item.setOrderInDay(maxOrder + 1);
                itemRepo.save(item);
            }

            log(itineraryId, userId, "ITIN_ITEM_UPDATED", Map.of("itemId", item.getId()));
            return toDto(item, item.getPlace().getName());
        }

        // ===== DELETE =====
        @Transactional
        public void deleteItem(String userId, String itineraryId, String itemId) {
            itineraryValidationService.validateItineraryEditable(itineraryId, userId);
            ensureCanEdit(userId, itineraryId);
            ItineraryItem item = itemRepo.findById(itemId)
                    .orElseThrow(() -> new InvalidDataException("Item không tồn tại"));
            if (!item.getItinerary().getId().equals(itineraryId)) {
                throw new InvalidDataException("Item không thuộc itinerary này");
            }
            itemRepo.delete(item);
            log(itineraryId, userId, "ITIN_ITEM_DELETED", Map.of("itemId", itemId));
        }

        @Transactional
        public void reorderInDay(String userId, String itineraryId, ReorderRequest req) {
            itineraryValidationService.validateItineraryEditable(itineraryId, userId);
            ensureCanEdit(userId, itineraryId);

            final int day = req.dayNumber(); // record accessor
            if (day < 1) throw new InvalidDataException("dayNumber >= 1");

            // Lấy các item của ngày theo thứ tự hiện tại
            List<ItineraryItem> items =
                    itemRepo.findByItinerary_IdAndDayNumberOrderByOrderInDayAsc(itineraryId, day);
            if (items.isEmpty()) return;

            Map<String, ItineraryItem> byId = items.stream()
                    .collect(Collectors.toMap(ItineraryItem::getId, it -> it));

            // 1) Set lại thứ tự theo danh sách client gửi
            int order = 1;
            for (String id : req.itemIdsInOrder()) {
                ItineraryItem it = byId.get(id);
                if (it == null) continue; // hoặc throw nếu cần strict
                it.setOrderInDay(order++);
            }

            // 2) Các item không có trong danh sách => giữ thứ tự cũ và append phía sau
            Set<String> provided = new HashSet<>(req.itemIdsInOrder());
            List<ItineraryItem> remaining = items.stream()
                    .filter(it -> !provided.contains(it.getId()))
                    .sorted(Comparator.comparingInt(ItineraryItem::getOrderInDay))
                    .collect(Collectors.toList());

            for (ItineraryItem r : remaining) {
                r.setOrderInDay(order++);
            }

            itemRepo.saveAll(items);
            log(itineraryId, userId, "ITIN_DAY_REORDERED", Map.of("day", day, "count", items.size()));
        }


        // ===== IMPORT BULK =====
        @Transactional
        public ImportPlacesResponse importPlaces(String userId, String itineraryId, ImportPlacesRequest req) {
            itineraryValidationService.validateItineraryEditable(itineraryId, userId);
            ensureCanEdit(userId, itineraryId);
            Itinerary itin = getItin(itineraryId);

            int defaultDay = defaultDay(req.getDefaultDay(), 1);
            boolean preventDup = Boolean.TRUE.equals(req.getPreventDuplicatesInDay());
            String mode = req.getAppendMode() == null ? "APPEND" : req.getAppendMode();

            int skipped = 0;
            List<ImportPlacesResponse.CreatedItem> created = new ArrayList<>();
            Map<Integer, Integer> maxOrderPerDay = new HashMap<>();
            Map<Integer, Set<String>> existedByDay = new HashMap<>();

            for (ImportPlacesRequest.ImportItem it : req.getItems()) {
                int day = defaultDay(it.getDayNumber(), defaultDay);
                validateDayInRange(itin, day);

                if (preventDup) {
                    existedByDay.computeIfAbsent(day, d -> new HashSet<>(itemRepo.findPlaceIdsInDay(itineraryId, d)));
                    if (existedByDay.get(day).contains(it.getPlaceId())) {
                        skipped++; continue;
                    }
                }

                Place place = placeRepo.findById(it.getPlaceId())
                        .orElseThrow(() -> new InvalidDataException("Place không tồn tại: " + it.getPlaceId()));

                Integer orderInDay = it.getOrderInDay();
                if (orderInDay == null || orderInDay < 1) {
                    int currentMax = maxOrderPerDay.computeIfAbsent(day,
                            d -> itemRepo.findMaxOrderInDayOrZero(itineraryId, d)); // <-- KHỚP REPO
                    orderInDay = currentMax + 1;
                    maxOrderPerDay.put(day, orderInDay);
                }

                LocalTime st = parseTime(it.getStartTime());
                LocalTime en = parseTime(it.getEndTime());
                if (st != null && en != null && !st.isBefore(en)) {
                    throw new InvalidDataException("startTime phải trước endTime");
                }

                ItineraryItem newItem = ItineraryItem.builder()
                        .itinerary(itin)
                        .place(place)
                        .dayNumber(day)
                        .orderInDay(orderInDay)
                        .startTime(st)
                        .endTime(en)
                        .description(it.getDescription())
                        .estimatedCost(it.getEstimatedCost())
                        .transportMode(parseTransport(it.getTransportMode()))
                        .build();

                itemRepo.save(newItem);
                if (preventDup) existedByDay.get(day).add(it.getPlaceId());

                created.add(ImportPlacesResponse.CreatedItem.builder()
                        .id(newItem.getId())
                        .placeId(place.getId())
                        .dayNumber(day)
                        .orderInDay(orderInDay)
                        .startTime(toStr(st))
                        .endTime(toStr(en))
                        .build());
            }

            if ("REINDEX".equalsIgnoreCase(mode)) {
                Set<Integer> touchedDays = created.stream()
                        .map(ImportPlacesResponse.CreatedItem::getDayNumber)
                        .collect(Collectors.toSet());
                for (Integer day : touchedDays) {
                    var all = itemRepo.findAllByItineraryAndDayForReindex(itineraryId, day); // <-- KHỚP REPO
                    int ord = 1; for (ItineraryItem it : all) it.setOrderInDay(ord++);
                    itemRepo.saveAll(all);
                }
            }

            log(itineraryId, userId, "IMPORT_PLACES",
                    Map.of("created", created.size(), "skipped", skipped, "mode", mode));

            return ImportPlacesResponse.builder()
                    .createdCount(created.size())
                    .skippedDuplicates(skipped)
                    .items(created)
                    .build();
        }

        // ===== LIST =====
        public List<ItineraryItemDto> listItems(String userId, String itineraryId, Integer dayNumber) {
            ensureCanView(userId, itineraryId);
            List<ItineraryItem> items = (dayNumber == null)
                    ? itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId) // <-- KHỚP REPO
                    : itemRepo.findByItinerary_IdAndDayNumberOrderByOrderInDayAsc(itineraryId, dayNumber);
            return items.stream().map(i -> toDto(i, i.getPlace().getName())).toList();
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
        private Itinerary getItin(String itineraryId) {
            return itineraryRepo.findById(itineraryId)
                    .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));
        }
        private Integer defaultDay(Integer v, int def) { return (v == null || v < 1) ? def : v; }
        private void validateDayInRange(Itinerary itin, int day) {
            LocalDate s = itin.getStartDate(), e = itin.getEndDate();
            if (s != null && e != null) {
                int max = (int)(e.toEpochDay() - s.toEpochDay()) + 1;
                if (day < 1 || day > max) throw new InvalidDataException("dayNumber ngoài phạm vi itinerary");
            }
        }
        private LocalTime parseTime(String s) { return (s == null || s.isBlank()) ? null : LocalTime.parse(s); }
        private String toStr(LocalTime t) { return t == null ? null : t.toString(); }
        private ItineraryItem.TransportMode parseTransport(String s) {
            if (s == null || s.isBlank()) return null;
            try { return ItineraryItem.TransportMode.valueOf(s.trim().toUpperCase()); } // <-- an toàn hơn
            catch (IllegalArgumentException ex) { throw new InvalidDataException("transportMode không hợp lệ"); }
        }
        private ItineraryItemDto toDto(ItineraryItem i, String placeName) {
            return ItineraryItemDto.builder()
                    .id(i.getId())
                    .placeId(i.getPlace().getId())
                    .placeName(placeName)
                    .dayNumber(i.getDayNumber())
                    .orderInDay(i.getOrderInDay())
                    .startTime(toStr(i.getStartTime()))
                    .endTime(toStr(i.getEndTime()))
                    .description(i.getDescription())
                    .estimatedCost(i.getEstimatedCost())
                    .transportMode(i.getTransportMode() == null ? null : i.getTransportMode().name())
                    .build();
        }
        private void log(String itineraryId, String userId, String action, Map<String, Object> details) {
            if (logService != null) logService.log(itineraryId, userId, action, details);
        }
    }
