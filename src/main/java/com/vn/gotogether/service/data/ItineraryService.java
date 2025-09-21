package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.CreateItineraryItemRequest;
import com.vn.gotogether.dto.data.CreateItineraryRequest;
import com.vn.gotogether.dto.data.ItineraryDetailResponse;
import com.vn.gotogether.dto.data.ItinerarySummaryResponse;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.data.PlaceRepository;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryItemRepository itemRepo;
    private final PlaceRepository placeRepo;
    private final UserRepository userRepo;

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
        ).toList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public ItineraryDetailResponse getItineraryDetail(String userId, String itineraryId) {
        Itinerary it = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));

        // chặn truy cập lịch trình của người khác
        if (it.getUser() == null || !userId.equals(it.getUser().getId())) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập lịch trình này");
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
        ).toList();

        return ItineraryDetailResponse.builder()
                .id(it.getId())
                .title(it.getTitle())
                .startDate(it.getStartDate())
                .endDate(it.getEndDate())
                .items(itemDtos)
                .build();
    }


    @Transactional
    public String createItinerary(String userId, CreateItineraryRequest req) {
        // ===== Validate input ngày chung
        if (req.getStartDate() == null || req.getEndDate() == null || req.getStartDate().isAfter(req.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu/kết thúc không hợp lệ");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }

        // ===== Tìm User
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        // ===== Tạo Itinerary
        Itinerary itin = Itinerary.builder()
                .user(user)
                .title(req.getTitle().trim())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .build();
        itin = itineraryRepo.save(itin);

        // ===== Thêm Items nếu có
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            List<ItineraryItem> toSave = new ArrayList<>();

            for (CreateItineraryItemRequest i : req.getItems()) {
                // ---- Validate phạm vi ngày
                if (i.getDayNumber() == null || i.getDayNumber() < 1 || i.getDayNumber() > days) {
                    throw new IllegalArgumentException("dayNumber không nằm trong phạm vi chuyến đi");
                }

                // ---- Tìm Place
                Place place = placeRepo.findById(i.getPlaceId())
                        .orElseThrow(() -> new IllegalArgumentException("Place không tồn tại: " + i.getPlaceId()));

                // ---- Validate time logic (nếu có)
                validateTimes(i.getStartTime(), i.getEndTime());

                // ---- Tính orderInDay (auto nếu null/<=0 hoặc bị trùng)
                int ord = (i.getOrderInDay() == null || i.getOrderInDay() <= 0)
                        ? nextOrderInDay(itin.getId(), i.getDayNumber())
                        : i.getOrderInDay();

                // Nếu trùng (itinerary_id, dayNumber, orderInDay) thì tự đẩy xuống vị trí kế tiếp
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

            // Lưu loạt cho hiệu năng
            itemRepo.saveAll(toSave);
        }

        return itin.getId();
    }

    // ================== Helpers ==================

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
}
