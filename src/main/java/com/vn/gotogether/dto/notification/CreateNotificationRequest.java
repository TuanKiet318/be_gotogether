package com.vn.gotogether.dto.notification;
import com.vn.gotogether.enums.EntityType;
import com.vn.gotogether.enums.NotificationType;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    private String userId; // Người nhận
    private String actorId; // Người thực hiện (có thể null)
    private NotificationType type;
    private EntityType entityType;
    private String entityId;
    private String content;
}