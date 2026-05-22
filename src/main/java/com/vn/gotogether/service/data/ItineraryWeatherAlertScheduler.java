package com.vn.gotogether.service.data;

import com.vn.gotogether.entity.Destination;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryCollaborator;
import com.vn.gotogether.enums.EntityType;
import com.vn.gotogether.enums.NotificationType;
import com.vn.gotogether.repository.data.ItineraryCollaboratorRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.data.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItineraryWeatherAlertScheduler {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryCollaboratorRepository collaboratorRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final WeatherForecastService weatherForecastService;

    @Value("${weather.alert.days-ahead:3}")
    private int alertDaysAhead;

    @Value("${weather.alert.zone:Asia/Ho_Chi_Minh}")
    private String alertZone;

    @Scheduled(cron = "${weather.alert.cron:0 0 8 * * *}", zone = "${weather.alert.zone:Asia/Ho_Chi_Minh}")
    public void notifyUpcomingItinerariesWeather() {
        System.out.println(">>> Scheduler RUNNING");
        ZoneId zoneId = ZoneId.of(alertZone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDate targetDate = today.plusDays(alertDaysAhead);
        System.out.println(targetDate);
        LocalDateTime from = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime to = LocalDateTime.of(today, LocalTime.MAX);
        List<Itinerary> itineraries = itineraryRepository.findByStartDate(targetDate);
        if (itineraries.isEmpty()) {
            return;
        }
        int sentCount = 0;

        for (Itinerary itinerary : itineraries) {
            Destination destination = itinerary.getDestination();
            if (destination == null || destination.getLat() == null || destination.getLon() == null) {
                continue;
            }

            var weatherOpt = weatherForecastService.getDailyWeatherAlert(
                    destination.getLat(),
                    destination.getLon(),
                    targetDate
            );
            System.out.println(">>> Scheduler RUNNING1");
            //|| (!weatherOpt.get().alertWorthy())
            if (weatherOpt.isEmpty() ) { 
                     System.out.println(">>> Scheduler RUNNING2");
                continue;
            }

            Set<String> recipientIds = new HashSet<>();
            recipientIds.add(itinerary.getUser().getId());

            List<ItineraryCollaborator> collaborators = collaboratorRepository.findByItineraryId(itinerary.getId());
            for (ItineraryCollaborator collaborator : collaborators) {
                recipientIds.add(collaborator.getUser().getId());
            }

            for (String recipientId : recipientIds) {
                boolean sentToday = notificationRepository
                        .existsByUserIdAndTypeAndEntityTypeAndEntityIdAndCreatedAtBetween(
                                recipientId,
                                NotificationType.WEATHER_ALERT,
                                EntityType.ITINERARY,
                                itinerary.getId(),
                                from,
                                to
                        );

                if (sentToday) {
                    continue;
                }

                notificationService.notifyItineraryWeatherAlert(
                        recipientId,
                        itinerary.getId(),
                        itinerary.getTitle(),
                        weatherOpt.get().summary()
                );
                sentCount++;
            }
        }

        log.info("Itinerary weather alert scheduler completed. TargetDate={}, sentCount={}", targetDate, sentCount);
    }
}
