package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Notification;
import com.vn.gotogether.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Lấy danh sách thông báo của user (phân trang)
    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.actor " +
            "WHERE n.user.id = :userId " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") String userId,
            Pageable pageable
    );

    // Lấy danh sách thông báo chưa đọc
    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.actor " +
            "WHERE n.user.id = :userId AND n.isRead = false " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(
            @Param("userId") String userId,
            Pageable pageable
    );

    // Đếm số thông báo chưa đọc
    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") String userId);

    // Đếm thông báo hôm nay
    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.user.id = :userId " +
            "AND n.createdAt >= :startOfDay")
    Long countTodayByUserId(
            @Param("userId") String userId,
            @Param("startOfDay") LocalDateTime startOfDay
    );

    // Đánh dấu tất cả đã đọc
    @Modifying
    @Query("UPDATE Notification n " +
            "SET n.isRead = true, n.readAt = :readAt " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(
            @Param("userId") String userId,
            @Param("readAt") LocalDateTime readAt
    );

    // Đánh dấu một thông báo đã đọc
    @Modifying
    @Query("UPDATE Notification n " +
            "SET n.isRead = true, n.readAt = :readAt " +
            "WHERE n.id = :notificationId AND n.user.id = :userId")
    int markAsRead(
            @Param("notificationId") String notificationId,
            @Param("userId") String userId,
            @Param("readAt") LocalDateTime readAt
    );

    // Xóa thông báo cũ
    @Modifying
    @Query("DELETE FROM Notification n " +
            "WHERE n.createdAt < :beforeDate")
    int deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);

    // Lọc theo loại thông báo
    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.actor " +
            "WHERE n.user.id = :userId AND n.type = :type " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndType(
            @Param("userId") String userId,
            @Param("type") NotificationType type,
            Pageable pageable
    );

    // Xóa thông báo liên quan đến entity (khi entity bị xóa)
    @Modifying
    @Query("DELETE FROM Notification n " +
            "WHERE n.entityId = :entityId AND n.entityType = :entityType")
    int deleteByEntity(
            @Param("entityId") String entityId,
            @Param("entityType") String entityType
    );
}
