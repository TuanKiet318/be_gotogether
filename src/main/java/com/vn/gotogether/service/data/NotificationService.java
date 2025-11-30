package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.notification.CreateNotificationRequest;
import com.vn.gotogether.dto.notification.NotificationResponse;
import com.vn.gotogether.dto.notification.NotificationStats;
import com.vn.gotogether.entity.Notification;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.enums.NotificationType;
import com.vn.gotogether.repository.data.NotificationRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketService;
    /**
     * Tạo thông báo mới
     */
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));

        User actor = null;
        if (request.getActorId() != null) {
            actor = userRepository.findById(request.getActorId())
                    .orElse(null);
        }

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .actor(actor)
                .type(request.getType())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .content(request.getContent())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Created notification: {} for user: {}", notification.getId(), user.getId());
        NotificationResponse response = toResponse(notification);
        // Gửi real-time notification qua WebSocket
        try {
            webSocketService.sendNotificationToUser(request.getUserId(), response);
            log.info("Sent real-time notification to user: {}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }

        return toResponse(notification);
    }

    /**
     * Lấy danh sách thông báo của user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toResponse);
    }

    /**
     * Lấy danh sách thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findUnreadByUserId(userId, pageable);
        return notifications.map(this::toResponse);
    }

    /**
     * Lấy thông báo theo loại
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByType(
            String userId,
            NotificationType type,
            Pageable pageable
    ) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndType(userId, type, pageable);
        return notifications.map(this::toResponse);
    }

    /**
     * Đánh dấu một thông báo đã đọc
     */
    @Transactional
    public void markAsRead(String notificationId, String userId) {
        int updated = notificationRepository.markAsRead(
                notificationId,
                userId,
                LocalDateTime.now()
        );

        if (updated > 0) {
            log.info("Marked notification {} as read for user {}", notificationId, userId);
        }
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @Transactional
    public int markAllAsRead(String userId) {
        int count = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    /**
     * Xóa thông báo
     */
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Deleted notification {} by user {}", notificationId, userId);
    }

    /**
     * Lấy thống kê thông báo
     */
    @Transactional(readOnly = true)
    public NotificationStats getStats(String userId) {
        Long unreadCount = notificationRepository.countUnreadByUserId(userId);

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayCount = notificationRepository.countTodayByUserId(userId, startOfDay);

        return NotificationStats.builder()
                .unreadCount(unreadCount)
                .todayCount(todayCount)
                .build();
    }

    /**
     * Xóa thông báo cũ (chạy định kỳ)
     */
    @Transactional
    public int deleteOldNotifications(int daysToKeep) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysToKeep);
        int count = notificationRepository.deleteOldNotifications(beforeDate);
        log.info("Deleted {} old notifications before {}", count, beforeDate);
        return count;
    }

    // Helper methods để tạo thông báo cho các sự kiện cụ thể

    /**
     * Tạo thông báo khi có người like blog
     */
    @Transactional
    public void notifyBlogLike(String blogOwnerId, String actorId, String blogId) {
        // Không tự thông báo cho chính mình
        if (blogOwnerId.equals(actorId)) {
            return;
        }

        User actor = userRepository.findById(actorId).orElse(null);
        String content = (actor != null ? actor.getName() : "Ai đó") + " đã thích bài viết của bạn";

        createNotification(CreateNotificationRequest.builder()
                .userId(blogOwnerId)
                .actorId(actorId)
                .type(NotificationType.BLOG_LIKE)
                .entityType(com.vn.gotogether.enums.EntityType.BLOG)
                .entityId(blogId)
                .content(content)
                .build());
    }

    /**
     * Tạo thông báo khi có người comment blog
     */
    @Transactional
    public void notifyBlogComment(String blogOwnerId, String actorId, String blogId) {
        if (blogOwnerId.equals(actorId)) {
            return;
        }

        User actor = userRepository.findById(actorId).orElse(null);
        String content = (actor != null ? actor.getName() : "Ai đó") + " đã bình luận về bài viết của bạn";

        createNotification(CreateNotificationRequest.builder()
                .userId(blogOwnerId)
                .actorId(actorId)
                .type(NotificationType.BLOG_COMMENT)
                .entityType(com.vn.gotogether.enums.EntityType.BLOG)
                .entityId(blogId)
                .content(content)
                .build());
    }

    /**
     * Tạo thông báo khi có người đăng ký tour
     */
    @Transactional
    public void notifyTourRegistration(String tourCreatorId, String participantId, String tourId, String tourTitle) {
        User participant = userRepository.findById(participantId).orElse(null);
        String content = (participant != null ? participant.getName() : "Ai đó") +
                " đã đăng ký tham gia tour \"" + tourTitle + "\"";

        createNotification(CreateNotificationRequest.builder()
                .userId(tourCreatorId)
                .actorId(participantId)
                .type(NotificationType.TOUR_REGISTRATION)
                .entityType(com.vn.gotogether.enums.EntityType.TOUR)
                .entityId(tourId)
                .content(content)
                .build());
    }

    /**
     * Tạo thông báo khi trạng thái tour thay đổi
     */
    @Transactional
    public void notifyTourStatusChange(String participantId, String tourId, String tourTitle, String newStatus) {
        String content = "Tour \"" + tourTitle + "\" đã chuyển sang trạng thái: " + newStatus;

        createNotification(CreateNotificationRequest.builder()
                .userId(participantId)
                .actorId(null)
                .type(NotificationType.TOUR_STATUS_CHANGE)
                .entityType(com.vn.gotogether.enums.EntityType.TOUR)
                .entityId(tourId)
                .content(content)
                .build());
    }

    /**
     * Tạo thông báo khi được mời vào itinerary
     */
    @Transactional
    public void notifyItineraryInvite(String invitedUserId, String inviterId, String itineraryId, String itineraryTitle) {
        User inviter = userRepository.findById(inviterId).orElse(null);
        String content = (inviter != null ? inviter.getName() : "Ai đó") +
                " đã mời bạn tham gia lịch trình \"" + itineraryTitle + "\"";

        createNotification(CreateNotificationRequest.builder()
                .userId(invitedUserId)
                .actorId(inviterId)
                .type(NotificationType.ITINERARY_INVITE)
                .entityType(com.vn.gotogether.enums.EntityType.ITINERARY)
                .entityId(itineraryId)
                .content(content)
                .build());
    }

    /**
     * Convert entity to response DTO
     */
    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse.ActorInfo actorInfo = null;
        if (notification.getActor() != null) {
            actorInfo = NotificationResponse.ActorInfo.builder()
                    .id(notification.getActor().getId())
                    .name(notification.getActor().getName())
                    .avatar(notification.getActor().getAvatar())
                    .build();
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .actor(actorInfo)
                .build();
    }
}