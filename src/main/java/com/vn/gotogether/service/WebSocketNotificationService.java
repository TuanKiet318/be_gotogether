package com.vn.gotogether.service;

import com.vn.gotogether.dto.itinerary.ItinerarySyncEvent;
import com.vn.gotogether.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo real-time đến user cụ thể
     */
    public void sendNotificationToUser(String userId, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
        );
    }

    /**
     * Gửi broadcast notification đến tất cả user
     */
    public void broadcastNotification(NotificationResponse notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification
        );
    }


    public void syncItineraryToRoom(String itineraryId, ItinerarySyncEvent event) {
        String destination = "/topic/itineraries/" + itineraryId;
        log.info("Sending realtime sync to {} - Action: {}", destination, event.getAction());

        messagingTemplate.convertAndSend(destination, event);
    }
}