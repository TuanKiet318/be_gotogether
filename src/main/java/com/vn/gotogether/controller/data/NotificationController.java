package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.notification.CreateNotificationRequest;
import com.vn.gotogether.dto.notification.NotificationResponse;
import com.vn.gotogether.dto.notification.NotificationStats;
import com.vn.gotogether.enums.NotificationType;
import com.vn.gotogether.service.data.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy danh sách thông báo của user hiện tại
     * GET /api/notifications?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = jwt.getClaimAsString("id");
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy danh sách thông báo chưa đọc
     * GET /api/notifications/unread?page=0&size=20
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = jwt.getClaimAsString("id");
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy thông báo theo loại
     * GET /api/notifications/type/BLOG_LIKE?page=0&size=20
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByType(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = jwt.getClaimAsString("id");
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getNotificationsByType(userId, type, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy thống kê thông báo
     * GET /api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<NotificationStats> getStats(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("id");
        NotificationStats stats = notificationService.getStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Đánh dấu một thông báo đã đọc
     * PUT /api/notifications/{notificationId}/read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String notificationId
    ) {
        String userId = jwt.getClaimAsString("id");
        notificationService.markAsRead(notificationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu thông báo đã đọc");
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("id");
        int count = notificationService.markAllAsRead(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã đánh dấu tất cả thông báo đã đọc");
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa thông báo
     * DELETE /api/notifications/{notificationId}
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String notificationId
    ) {
        String userId = jwt.getClaimAsString("id");
        notificationService.deleteNotification(notificationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xóa thông báo");
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo thông báo (API nội bộ - có thể bảo vệ bằng role ADMIN)
     * POST /api/notifications
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody CreateNotificationRequest request
    ) {
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.ok(notification);
    }
}