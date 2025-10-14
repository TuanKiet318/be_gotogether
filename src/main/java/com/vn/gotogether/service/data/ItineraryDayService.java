package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.ItineraryDayResponse;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryItem;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryDayService {

    private final ItineraryRepository itineraryRepo;
    private final ItineraryItemRepository itemRepo;
    private final PermissionService permissionService;

    @Transactional
    public ItineraryDayResponse insertBefore(String userId, String itineraryId, int dayNumber, int count) {
        ensureCanEdit(userId, itineraryId);
        Itinerary itin = getItinerary(itineraryId);

        int totalDays = calculateTotalDays(itin);
        if (dayNumber < 1 || dayNumber > totalDays) {
            throw new InvalidDataException("dayNumber phải trong khoảng 1-" + totalDays);
        }
        if (count < 1) count = 1;

        List<ItineraryItem> itemsToShift = itemRepo.findByItinerary_IdAndDayNumberGreaterThanEqualOrderByDayNumberAsc(
                itineraryId, dayNumber);

        for (ItineraryItem item : itemsToShift) {
            item.setDayNumber(item.getDayNumber() + count);
        }
        itemRepo.saveAll(itemsToShift);

        LocalDate newStartDate = itin.getStartDate().minusDays(count);
        itin.setStartDate(newStartDate);
        itineraryRepo.save(itin);

        log.info("Inserted {} day(s) before day {} in itinerary {}", count, dayNumber, itineraryId);

        return ItineraryDayResponse.builder()
                .itineraryId(itineraryId)
                .message("Đã thêm " + count + " ngày trước ngày " + dayNumber)
                .newStartDate(itin.getStartDate())
                .newEndDate(itin.getEndDate())
                .totalDays(calculateTotalDays(itin))
                .itemsAffected(itemsToShift.size())
                .itemsDeleted(0)
                .build();
    }

    @Transactional
    public ItineraryDayResponse insertAfter(String userId, String itineraryId, int dayNumber, int count) {
        ensureCanEdit(userId, itineraryId);
        Itinerary itin = getItinerary(itineraryId);

        int totalDays = calculateTotalDays(itin);
        if (dayNumber < 1 || dayNumber > totalDays) {
            throw new InvalidDataException("dayNumber phải trong khoảng 1-" + totalDays);
        }
        if (count < 1) count = 1;

        List<ItineraryItem> itemsToShift = itemRepo.findByItinerary_IdAndDayNumberGreaterThanEqualOrderByDayNumberAsc(
                itineraryId, dayNumber + 1);

        for (ItineraryItem item : itemsToShift) {
            item.setDayNumber(item.getDayNumber() + count);
        }
        itemRepo.saveAll(itemsToShift);

        LocalDate newEndDate = itin.getEndDate().plusDays(count);
        itin.setEndDate(newEndDate);
        itineraryRepo.save(itin);

        log.info("Inserted {} day(s) after day {} in itinerary {}", count, dayNumber, itineraryId);

        return ItineraryDayResponse.builder()
                .itineraryId(itineraryId)
                .message("Đã thêm " + count + " ngày sau ngày " + dayNumber)
                .newStartDate(itin.getStartDate())
                .newEndDate(itin.getEndDate())
                .totalDays(calculateTotalDays(itin))
                .itemsAffected(itemsToShift.size())
                .itemsDeleted(0)
                .build();
    }

    @Transactional
    public ItineraryDayResponse removeDay(String userId, String itineraryId, int dayNumber) {
        ensureCanEdit(userId, itineraryId);
        Itinerary itin = getItinerary(itineraryId);

        int totalDays = calculateTotalDays(itin);
        if (totalDays <= 1) {
            throw new InvalidDataException("Không thể xóa ngày duy nhất trong lịch trình");
        }
        if (dayNumber < 1 || dayNumber > totalDays) {
            throw new InvalidDataException("dayNumber phải trong khoảng 1-" + totalDays);
        }

        List<ItineraryItem> itemsToDelete = itemRepo.findByItinerary_IdAndDayNumber(itineraryId, dayNumber);
        itemRepo.deleteAll(itemsToDelete);
        int deletedCount = itemsToDelete.size();

        List<ItineraryItem> itemsToShift = itemRepo.findByItinerary_IdAndDayNumberGreaterThanEqualOrderByDayNumberAsc(
                itineraryId, dayNumber + 1);

        for (ItineraryItem item : itemsToShift) {
            item.setDayNumber(item.getDayNumber() - 1);
        }
        itemRepo.saveAll(itemsToShift);

        if (dayNumber == 1) {
            itin.setStartDate(itin.getStartDate().plusDays(1));
        } else if (dayNumber == totalDays) {
            itin.setEndDate(itin.getEndDate().minusDays(1));
        } else {
            itin.setEndDate(itin.getEndDate().minusDays(1));
        }
        itineraryRepo.save(itin);

        log.info("Removed day {} from itinerary {}, deleted {} items", dayNumber, itineraryId, deletedCount);

        return ItineraryDayResponse.builder()
                .itineraryId(itineraryId)
                .message("Đã xóa ngày " + dayNumber)
                .newStartDate(itin.getStartDate())
                .newEndDate(itin.getEndDate())
                .totalDays(calculateTotalDays(itin))
                .itemsAffected(itemsToShift.size())
                .itemsDeleted(deletedCount)
                .build();
    }

    private void ensureCanEdit(String userId, String itineraryId) {
        if (!permissionService.canEdit(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa itinerary này");
        }
    }

    private Itinerary getItinerary(String itineraryId) {
        return itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Itinerary không tồn tại"));
    }

    private int calculateTotalDays(Itinerary itin) {
        if (itin.getStartDate() == null || itin.getEndDate() == null) return 0;
        return (int) ChronoUnit.DAYS.between(itin.getStartDate(), itin.getEndDate()) + 1;
    }
}