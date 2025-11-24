package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.ItineraryMedia;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.ItineraryMediaRepository;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryMediaService {

    private final ItineraryMediaRepository mediaRepo;
    private final ItineraryRepository itineraryRepo;
    private final UserRepository userRepo;
    private final PermissionService permissionService;

    @Transactional
    public ItineraryMediaDto uploadMedia(String userId, String itineraryId, UploadMediaRequest req) {
        // Kiểm tra quyền edit
        if (!permissionService.canEdit(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền upload media cho lịch trình này");
        }

        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        // Validate dayNumber
        if (req.getDayNumber() != null) {
            long totalDays = ChronoUnit.DAYS.between(itinerary.getStartDate(), itinerary.getEndDate()) + 1;
            if (req.getDayNumber() < 1 || req.getDayNumber() > totalDays) {
                throw new InvalidDataException("Day number phải từ 1 đến " + totalDays);
            }
        }

        // Validate mediaType
        ItineraryMedia.MediaType mediaType;
        try {
            mediaType = ItineraryMedia.MediaType.valueOf(req.getMediaType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Media type không hợp lệ. Chỉ chấp nhận IMAGE hoặc VIDEO");
        }

        // Tính orderInDay
        Integer orderInDay = req.getDayNumber() != null
                ? mediaRepo.findMaxOrderInDay(itineraryId, req.getDayNumber()) + 1
                : null;

        ItineraryMedia media = ItineraryMedia.builder()
                .itinerary(itinerary)
                .user(user)
                .dayNumber(req.getDayNumber())
                .mediaType(mediaType)
                .mediaUrl(req.getMediaUrl())
                .thumbnailUrl(req.getThumbnailUrl())
                .caption(req.getCaption())
                .fileSize(req.getFileSize())
                .duration(req.getDuration())
                .width(req.getWidth())
                .height(req.getHeight())
                .orderInDay(orderInDay)
                .build();

        media = mediaRepo.save(media);

        return mapToDto(media);
    }

    public List<ItineraryMediaDto> getAllMedia(String userId, String itineraryId) {
        // Kiểm tra quyền view
        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem media của lịch trình này");
        }

        return mediaRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ItineraryMediaDto> getMediaByDay(String userId, String itineraryId, Integer dayNumber) {
        // Kiểm tra quyền view
        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem media của lịch trình này");
        }

        return mediaRepo.findByItinerary_IdAndDayNumberOrderByOrderInDayAsc(itineraryId, dayNumber)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItineraryMediaDto updateMedia(String userId, String itineraryId, String mediaId, UpdateMediaRequest req) {
        // Kiểm tra quyền edit
        if (!permissionService.canEdit(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa media của lịch trình này");
        }

        ItineraryMedia media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new InvalidDataException("Media không tồn tại"));

        if (!media.getItinerary().getId().equals(itineraryId)) {
            throw new InvalidDataException("Media không thuộc lịch trình này");
        }

        if (req.getCaption() != null) {
            media.setCaption(req.getCaption());
        }

        if (req.getDayNumber() != null) {
            Itinerary itinerary = media.getItinerary();
            long totalDays = ChronoUnit.DAYS.between(itinerary.getStartDate(), itinerary.getEndDate()) + 1;
            if (req.getDayNumber() < 1 || req.getDayNumber() > totalDays) {
                throw new InvalidDataException("Day number phải từ 1 đến " + totalDays);
            }
            media.setDayNumber(req.getDayNumber());
        }

        if (req.getOrderInDay() != null) {
            media.setOrderInDay(req.getOrderInDay());
        }

        media = mediaRepo.save(media);
        return mapToDto(media);
    }

    @Transactional
    public void deleteMedia(String userId, String itineraryId, String mediaId) {
        // Kiểm tra quyền edit
        if (!permissionService.canEdit(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa media của lịch trình này");
        }

        ItineraryMedia media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new InvalidDataException("Media không tồn tại"));

        if (!media.getItinerary().getId().equals(itineraryId)) {
            throw new InvalidDataException("Media không thuộc lịch trình này");
        }

        mediaRepo.delete(media);
    }

    public MediaStatsDto getMediaStats(String userId, String itineraryId) {
        // Kiểm tra quyền view
        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền xem thống kê media");
        }

        long totalMedia = mediaRepo.countByItinerary_Id(itineraryId);
        long totalImages = mediaRepo.countByItinerary_IdAndMediaType(itineraryId, ItineraryMedia.MediaType.IMAGE);
        long totalVideos = mediaRepo.countByItinerary_IdAndMediaType(itineraryId, ItineraryMedia.MediaType.VIDEO);

        List<ItineraryMedia> allMedia = mediaRepo.findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);
        int daysWithMedia = (int) allMedia.stream()
                .map(ItineraryMedia::getDayNumber)
                .filter(day -> day != null)
                .distinct()
                .count();

        return MediaStatsDto.builder()
                .totalMedia(totalMedia)
                .totalImages(totalImages)
                .totalVideos(totalVideos)
                .daysWithMedia(daysWithMedia)
                .build();
    }

    private ItineraryMediaDto mapToDto(ItineraryMedia media) {
        return ItineraryMediaDto.builder()
                .id(media.getId())
                .itineraryId(media.getItinerary().getId())
                .userId(media.getUser().getId())
                .userName(media.getUser().getName())
                .userAvatar(media.getUser().getAvatar())
                .dayNumber(media.getDayNumber())
                .mediaType(media.getMediaType().name())
                .mediaUrl(media.getMediaUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .caption(media.getCaption())
                .fileSize(media.getFileSize())
                .duration(media.getDuration())
                .width(media.getWidth())
                .height(media.getHeight())
                .orderInDay(media.getOrderInDay())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build();
    }
}