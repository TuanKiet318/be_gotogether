package com.vn.gotogether.dto.notification;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStats {
    private Long totalCount;
    private Long unreadCount;
    private Long todayCount;
}