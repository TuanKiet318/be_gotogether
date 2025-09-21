package com.vn.gotogether.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryActivityLog;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.repository.data.ItineraryActivityLogRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ItineraryActivityLogRepository logRepo;
    private final ItineraryRepository itineraryRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper; // Spring Boot auto-config

    public void log(String itineraryId, String actorUserId, String action, Map<String, Object> details) {
        Itinerary itin = itineraryRepo.getReferenceById(itineraryId);
        User actor = (actorUserId == null) ? null : userRepo.getReferenceById(actorUserId);

        String detailsJson = null;
        try {
            if (details != null && !details.isEmpty()) {
                detailsJson = objectMapper.writeValueAsString(details);
            }
        } catch (Exception ignore) {
            // không chặn flow nghiệp vụ nếu serialize log lỗi
        }

        ItineraryActivityLog log = ItineraryActivityLog.builder()
                .itinerary(itin)
                .actor(actor)
                .action(action)
                .detailsJson(detailsJson)
                .build();

        logRepo.save(log);
    }
}
