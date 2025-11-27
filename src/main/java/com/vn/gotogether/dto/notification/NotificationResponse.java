package com.vn.gotogether.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vn.gotogether.enums.EntityType;
import com.vn.gotogether.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private NotificationType type;
    private EntityType entityType;
    private String entityId;
    private String content;
    private Boolean isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Thông tin người thực hiện hành động
    private ActorInfo actor;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActorInfo {
        private String id;
        private String name;
        private String avatar;
    }
}
