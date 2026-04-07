package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.dto.itinerary.ItinerarySyncEvent;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.WebSocketNotificationService;
import com.vn.gotogether.service.data.ItineraryItemService;
import com.vn.gotogether.service.data.ItineraryService;
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

    private final ItineraryItemService itineraryService;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    // ====== CREATE ======
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryItemDto createItem(@PathVariable String itineraryId,
                                       @Valid @RequestBody CreateItemRequest req) {
        String userId = currentUserId();
        ItineraryItemDto savedItem = itineraryService.createItem(userId, itineraryId, req);

        // Bắn sự kiện realtime
        ItinerarySyncEvent event = new ItinerarySyncEvent("CREATE", itineraryId, userId, savedItem);
        webSocketNotificationService.syncItineraryToRoom(itineraryId, event);

        return savedItem;
    }

    // ====== UPDATE ======
    @PatchMapping("/{itemId}")
    public ItineraryItemDto updateItem(@PathVariable String itineraryId,
                                       @PathVariable String itemId,
                                       @Valid @RequestBody UpdateItemRequest req) {
        String userId = currentUserId();
        ItineraryItemDto updatedItem = itineraryService.updateItem(userId, itineraryId, itemId, req);

        // Bắn sự kiện realtime
        ItinerarySyncEvent event = new ItinerarySyncEvent("UPDATE", itineraryId, userId, updatedItem);
        webSocketNotificationService.syncItineraryToRoom(itineraryId, event);

        return updatedItem;
    }

    // ====== DELETE ======
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable String itineraryId,
                           @PathVariable String itemId) {
        String userId = currentUserId();
        itineraryService.deleteItem(userId, itineraryId, itemId);

        // Bắn sự kiện realtime (data lúc này chỉ cần gửi itemId bị xóa)
        ItinerarySyncEvent event = new ItinerarySyncEvent("DELETE", itineraryId, userId, itemId);
        webSocketNotificationService.syncItineraryToRoom(itineraryId, event);
    }

    // ====== REORDER ======
    @PostMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderInDay(@PathVariable String itineraryId,
                             @Valid @RequestBody ReorderRequest req) {
        String userId = currentUserId();
        itineraryService.reorderInDay(userId, itineraryId, req);
    }

    // ====== IMPORT (bulk) ======
    @PostMapping("/import")
    public ImportPlacesResponse importPlaces(@PathVariable String itineraryId,
                                             @Valid @RequestBody ImportPlacesRequest req) {
        String userId = currentUserId();
        // 🔥 SỬA LẠI THỨ TỰ THAM SỐ
        return itineraryService.importPlaces(userId, itineraryId, req);
    }

    // ====== LIST ======
    @GetMapping
    public List<ItineraryItemDto> listItems(@PathVariable String itineraryId,
                                            @RequestParam(required = false) Integer dayNumber) {
        String userId = currentUserId();
        return itineraryService.listItems(userId, itineraryId, dayNumber);
    }

    // Helper: lấy user hiện tại từ SecurityContext
    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        return user.getId();
    }
}
