package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryService; // hoặc ItineraryItemService nếu bạn tách service
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries/{itineraryId}/items")
@RequiredArgsConstructor
public class ItineraryItemController {

    private final ItineraryService itineraryService; // hoặc ItineraryItemService
    private final UserRepository userRepository;

    // ====== CREATE: thêm 1 địa điểm (item) vào lịch trình ======
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryItemDto createItem(@PathVariable String itineraryId,
                                       @Valid @RequestBody CreateItemRequest req) {
        String userId = currentUserId();
        return itineraryService.createItem(userId, itineraryId, req);
    }

    // ====== UPDATE: chỉnh giờ/mô tả/move day/order… của 1 item ======
    @PatchMapping("/{itemId}")
    public ItineraryItemDto updateItem(@PathVariable String itineraryId,
                                       @PathVariable String itemId,
                                       @Valid @RequestBody UpdateItemRequest req) {
        String userId = currentUserId();
        return itineraryService.updateItem(userId, itineraryId, itemId, req);
    }

    // ====== DELETE: xoá 1 item ======
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable String itineraryId,
                           @PathVariable String itemId) {
        String userId = currentUserId();
        itineraryService.deleteItem(userId, itineraryId, itemId);
    }

    // ====== REORDER: sắp xếp lại thứ tự trong 1 ngày ======
    @PostMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderInDay(@PathVariable String itineraryId,
                             @Valid @RequestBody ReorderRequest req) {
        String userId = currentUserId();
        itineraryService.reorderInDay(userId, itineraryId, req);
    }

    // ====== IMPORT: thêm nhiều địa điểm vào lịch trình (bulk) ======
    @PostMapping("/import")
    public ImportPlacesResponse importPlaces(@PathVariable String itineraryId,
                                             @Valid @RequestBody ImportPlacesRequest req) {
        String userId = currentUserId();
        return itineraryService.importPlaces(itineraryId, userId, req);
    }

    // ====== LIST: liệt kê items (tuỳ chọn: lọc theo dayNumber) ======
    @GetMapping
    public List<ItineraryItemDto> listItems(@PathVariable String itineraryId,
                                            @RequestParam(required = false) Integer dayNumber) {
        String userId = currentUserId();
        return itineraryService.listItems(userId, itineraryId, dayNumber);
    }

    // Helper lấy current userId từ SecurityContext
    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        return user.getId();
    }
}
