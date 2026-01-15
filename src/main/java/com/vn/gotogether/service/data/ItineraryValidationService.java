package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.WarningDto;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.exception.UnauthorizedException;
import com.vn.gotogether.repository.data.CategoryDefaultRepository;
import com.vn.gotogether.repository.data.OpeningHourRepository;
import com.vn.gotogether.repository.data.PlaceRepository;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.data.ItineraryCollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItineraryValidationService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryItemRepository itemRepo;
    private final PlaceRepository placeRepo;
    private final OpeningHourRepository openingHourRepository;
    private final CategoryDefaultRepository categoryDefaultRepository;
    private final ItineraryCollaboratorRepository collaboratorRepository;
    private final com.vn.gotogether.service.HaversineTravelTimeProvider travelTimeProvider;

    private static final int MIN_TRAVEL_BUFFER_MIN = 10;
    private static final double MIN_VISIT_BUFFER_PERCENT = 0.5;

    // ================== VALIDATION CHO CHỈNH SỬA ITINERARY ==================

    /**
     * Kiểm tra user có quyền chỉnh sửa itinerary không
     */
    @Transactional(readOnly = true)
    public boolean canEditItinerary(String itineraryId, String userId) {
        // 1. Lấy itinerary
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        // 2. Kiểm tra owner (user trong itinerary entity)
        if (itinerary.getUser().getId().equals(userId)) {
            return true;
        }

        // 3. Kiểm tra collaborator với role EDITOR
        return collaboratorRepository.existsByItineraryIdAndUserIdAndRole(
                itineraryId,
                userId,
                ItineraryCollaborator.Role.EDITOR
        );
    }

    /**
     * Kiểm tra user có quyền xem itinerary không
     */
    @Transactional(readOnly = true)
    public boolean canViewItinerary(String itineraryId, String userId) {
        // 1. Lấy itinerary
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        // 2. Kiểm tra public
        if (itinerary.isPublic()) {
            return true;
        }

        // 3. Kiểm tra owner
        if (itinerary.getUser().getId().equals(userId)) {
            return true;
        }

        // 4. Kiểm tra collaborator (EDITOR hoặc VIEWER)
        return collaboratorRepository.findByItineraryIdAndUserId(itineraryId, userId).isPresent();
    }

    /**
     * Kiểm tra itinerary có đang ở trạng thái có thể chỉnh sửa không (chưa qua ngày kết thúc)
     */
    @Transactional(readOnly = true)
    public boolean isItineraryEditableByTime(String itineraryId) {
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        return isItineraryEditableByTime(itinerary);
    }

    /**
     * Overload method với timezone
     */
    @Transactional(readOnly = true)
    public boolean isItineraryEditableByTime(String itineraryId, String timezone) {
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        return isItineraryEditableByTime(itinerary, timezone);
    }

    /**
     * Kiểm tra dựa trên entity itinerary
     */
    public boolean isItineraryEditableByTime(Itinerary itinerary) {
        return isItineraryEditableByTime(itinerary, "Asia/Ho_Chi_Minh");
    }

    public boolean isItineraryEditableByTime(Itinerary itinerary, String timezone) {
        if (itinerary.getEndDate() == null) {
            return true; // Itinerary chưa có end date có thể chỉnh sửa
        }

        ZoneId zone = ZoneId.of(timezone != null && !timezone.isBlank() ? timezone : "Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zone);

        // Chỉnh sửa được khi: today <= endDate (chưa qua ngày kết thúc)
        return !today.isAfter(itinerary.getEndDate());
    }

    /**
     * Validate tổng hợp - kiểm tra cả permission và thời gian
     * @throws UnauthorizedException nếu không có quyền
     * @throws ResponseStatusException nếu itinerary đã qua thời gian
     */
    @Transactional(readOnly = true)
    public void validateItineraryEditable(String itineraryId, String userId) {
        validateItineraryEditable(itineraryId, userId, "Asia/Ho_Chi_Minh");
    }

    @Transactional(readOnly = true)
    public void validateItineraryEditable(String itineraryId, String userId, String timezone) {
        // 1. Kiểm tra permission
        if (!canEditItinerary(itineraryId, userId)) {
            throw new UnauthorizedException("Bạn không có quyền chỉnh sửa lịch trình này");
        }

        // 2. Kiểm tra thời gian
        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));

        if (!isItineraryEditableByTime(itinerary, timezone)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Không thể chỉnh sửa lịch trình đã qua thời gian thực hiện"
            );
        }
    }

    /**
     * Lấy trạng thái thời gian của itinerary
     */
    public ItineraryTimeStatus getItineraryTimeStatus(Itinerary itinerary, String timezone) {
        if (itinerary.getStartDate() == null || itinerary.getEndDate() == null) {
            return ItineraryTimeStatus.UPCOMING;
        }

        ZoneId zone = ZoneId.of(timezone != null && !timezone.isBlank() ? timezone : "Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zone);

        LocalDate startDate = itinerary.getStartDate();
        LocalDate endDate = itinerary.getEndDate();

        if (today.isAfter(endDate)) {
            return ItineraryTimeStatus.PAST;
        } else if (today.isBefore(startDate)) {
            return ItineraryTimeStatus.UPCOMING;
        } else {
            return ItineraryTimeStatus.ONGOING;
        }
    }

    /**
     * Enum cho trạng thái thời gian
     */
    public enum ItineraryTimeStatus {
        PAST,      // Đã qua
        ONGOING,   // Đang diễn ra
        UPCOMING   // Sắp tới
    }

    // ================== VALIDATION WARNINGS (CODE CŨ GIỮ NGUYÊN) ==================

    /**
     * Validate itinerary and return warnings grouped by dayNumber.
     * Only days that have at least one warning are included in the returned map.
     */
    public Map<Integer, List<WarningDto>> validateItineraryByDay(String itineraryId, String timezone) {
        Itinerary itin = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));

        LocalDate startDate = itin.getStartDate();
        ZoneId zone = (timezone == null || timezone.isBlank()) ? ZoneId.of("Asia/Ho_Chi_Minh") : ZoneId.of(timezone);

        List<ItineraryItem> items = itemRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);
        Map<Integer, List<WarningDto>> byDay = new LinkedHashMap<>();
        if (items.isEmpty()) return byDay;

        // preload places
        Set<String> placeIds = items.stream().map(i -> i.getPlace().getId()).collect(Collectors.toSet());
        Map<String, Place> placeMap = placeRepo.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, p -> p));

        final int BUFFER = MIN_TRAVEL_BUFFER_MIN;
        final double MIN_VISIT_PERCENT = MIN_VISIT_BUFFER_PERCENT;

        for (int i = 0; i < items.size(); i++) {
            ItineraryItem item = items.get(i);
            int day = (item.getDayNumber() == null) ? 1 : item.getDayNumber();
            List<WarningDto> list = byDay.computeIfAbsent(day, d -> new ArrayList<>());

            String itemId = item.getId();
            Place place = placeMap.get(item.getPlace().getId());
            if (place == null) {
                list.add(w(itemId, WarningDto.Type.MISSING_DATA, "Place not found"));
                continue;
            }

            LocalDate itemDate = startDate.plusDays(Math.max(1, day) - 1);
            LocalTime start = item.getStartTime();
            LocalTime end = item.getEndTime();

            // 1) Opening hours: date-specific -> weekday -> category default
            if (start != null && end != null) {
                List<OpeningHour> dateSpecific = openingHourRepository.findByPlaceIdAndDate(place.getId(), itemDate);
                if (!dateSpecific.isEmpty()) {
                    boolean ok = dateSpecific.stream().anyMatch(oh -> !start.isBefore(oh.getOpenTime()) && !end.isAfter(oh.getCloseTime()));
                    if (!ok) {
                        boolean partial = dateSpecific.stream().anyMatch(oh -> start.isBefore(oh.getCloseTime()) && end.isAfter(oh.getOpenTime()));
                        list.add(w(itemId, partial ? WarningDto.Type.PARTIAL_OPEN : WarningDto.Type.CLOSED,
                                String.format("'%s' có giờ override %s-%s. Lịch %s-%s không phù hợp.",
                                        place.getName(), dateSpecific.get(0).getOpenTime(), dateSpecific.get(0).getCloseTime(), start, end)));
                    }
                } else {
                    int weekday = itemDate.getDayOfWeek().getValue();
                    List<OpeningHour> wk = openingHourRepository.findByPlaceIdAndWeekday(place.getId(), weekday);
                    if (!wk.isEmpty()) {
                        boolean ok = wk.stream().anyMatch(oh -> !start.isBefore(oh.getOpenTime()) && !end.isAfter(oh.getCloseTime()));
                        if (!ok) {
                            boolean partial = wk.stream().anyMatch(oh -> start.isBefore(oh.getCloseTime()) && end.isAfter(oh.getOpenTime()));
                            list.add(w(itemId, partial ? WarningDto.Type.PARTIAL_OPEN : WarningDto.Type.CLOSED,
                                    String.format("'%s' theo lịch tuần có giờ %s-%s. Lịch %s-%s không phù hợp.",
                                            place.getName(), wk.get(0).getOpenTime(), wk.get(0).getCloseTime(), start, end)));
                        }
                    } else {
                        categoryDefaultRepository.findByCategoryId(place.getCategory().getId()).ifPresent(cat -> {
                            if (cat.getDefaultOpen() != null && cat.getDefaultClose() != null) {
                                if (start.isBefore(cat.getDefaultOpen()) || end.isAfter(cat.getDefaultClose())) {
                                    boolean partial = start.isBefore(cat.getDefaultClose()) && end.isAfter(cat.getDefaultOpen());
                                    list.add(w(itemId, partial ? WarningDto.Type.PARTIAL_OPEN : WarningDto.Type.CLOSED,
                                            String.format("Không có giờ cụ thể của '%s'. Áp giờ category %s-%s. Lịch %s-%s không phù hợp.",
                                                    place.getName(), cat.getDefaultOpen(), cat.getDefaultClose(), start, end)));
                                }
                            } else if (cat.getDefaultVisitMinutes() != null) {
                                long scheduled = Duration.between(start, end).toMinutes();
                                if (scheduled < cat.getDefaultVisitMinutes() * MIN_VISIT_PERCENT) {
                                    list.add(w(itemId, WarningDto.Type.SHORT_VISIT,
                                            String.format("Thời gian ở '%s' (%d phút) rất ngắn so với chuẩn category (%d phút).",
                                                    place.getName(), scheduled, cat.getDefaultVisitMinutes())));
                                }
                            }
                        });
                    }
                }
            } else {
                // no fixed time: warn only if no opening & no category default
                List<OpeningHour> wk = openingHourRepository.findByPlaceIdAndWeekday(place.getId(), itemDate.getDayOfWeek().getValue());
                if (wk.isEmpty()) {
                    boolean hasCat = categoryDefaultRepository.findByCategoryId(place.getCategory().getId()).isPresent();
                    if (!hasCat) {
                        list.add(w(itemId, WarningDto.Type.MISSING_DATA,
                                "Không có giờ mở cụ thể cho địa điểm và không có default category"));
                    }
                }
            }

            // 2) Travel to next (cross-day handled using timezone)
            if (i < items.size() - 1) {
                ItineraryItem next = items.get(i + 1);
                Place nextPlace = placeMap.get(next.getPlace().getId());
                if (nextPlace != null && item.getEndTime() != null && next.getStartTime() != null) {
                    int travelMin = travelTimeProvider.estimateTravelMinutes(place.getLat(), place.getLon(),
                            nextPlace.getLat(), nextPlace.getLon(), item.getTransportMode() == null ? null : item.getTransportMode().name());
                    LocalDate nextDate = startDate.plusDays(Math.max(1, next.getDayNumber()) - 1);
                    Instant curEnd = item.getEndTime().atDate(itemDate).atZone(zone).toInstant();
                    Instant nextStart = next.getStartTime().atDate(nextDate).atZone(zone).toInstant();
                    long available = Duration.between(curEnd, nextStart).toMinutes();
                    if (available < (travelMin + BUFFER)) {
                        list.add(w(itemId, WarningDto.Type.NOT_ENOUGH_TRAVEL,
                                String.format("Giữa '%s' → '%s' cần khoảng %d phút nhưng chỉ có khoảng %d phút trống.",
                                        place.getName(), nextPlace.getName(), travelMin, BUFFER, available)));
                    }
                }
            }
        }

        // remove empty day entries: only keep days with non-empty lists
        Map<Integer, List<WarningDto>> filtered = byDay.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));

        return filtered;
    }

    private WarningDto w(String itemId, WarningDto.Type type, String msg) {
        return WarningDto.builder().itemId(itemId).type(type).message(msg).build();
    }
}