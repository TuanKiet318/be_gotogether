package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.CreateTourRequest;
import com.vn.gotogether.dto.data.TourDetailResponse;
import com.vn.gotogether.dto.data.UpdateTourRequest;
import com.vn.gotogether.entity.Itinerary;
import com.vn.gotogether.entity.Tour;
import com.vn.gotogether.entity.TourParticipant;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.model.TourStatus;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.data.TourParticipantRepository;
import com.vn.gotogether.repository.data.TourRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TourService {

    private final TourRepository tourRepo;
    private final TourParticipantRepository participantRepo;
    private final ItineraryRepository itineraryRepo;

    /** Tạo tour từ itinerary */
    public Tour createTour(CreateTourRequest req, User creator) {
        Itinerary itinerary = itineraryRepo.findById(req.getItineraryId())
                .orElseThrow(() -> new EntityNotFoundException("Itinerary not found"));

        Tour tour = Tour.builder()
                .itinerary(itinerary)
                .creator(creator)
                .title(req.getTitle())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .registrationDeadline(req.getRegistrationDeadline())
                .maxParticipants(req.getMaxParticipants())
                .pricePerPerson(req.getPricePerPerson())
                .build();

        return tourRepo.save(tour);
    }

    /** Lấy chi tiết tour */
    public TourDetailResponse getTourDetail(String tourId) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        return TourDetailResponse.builder()
                .id(tour.getId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .startDate(tour.getStartDate())
                .endDate(tour.getEndDate())
                .registrationDeadline(tour.getRegistrationDeadline())
                .maxParticipants(tour.getMaxParticipants())
                .currentParticipants(tour.getCurrentParticipants())
                .pricePerPerson(tour.getPricePerPerson())
                .status(tour.getStatus())
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .itinerary(mapItineraryInfo(tour.getItinerary()))
                .creator(mapUserInfo(tour.getCreator()))
                .participants(tour.getParticipants() != null ?
                        tour.getParticipants().stream()
                                .map(this::mapParticipantInfo)
                                .collect(Collectors.toList()) :
                        null)
                .build();
    }

    /** Lấy danh sách tours với phân trang và filter */
    public Page<TourDetailResponse> listTours(
            TourStatus status,
            String creatorId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            Pageable pageable) {

        Page<Tour> tours;

        // Nếu có filter
        if (status != null || creatorId != null || startDateFrom != null || startDateTo != null) {
            tours = tourRepo.findWithFilters(status, creatorId, startDateFrom, startDateTo, pageable);
        } else {
            tours = tourRepo.findAll(pageable);
        }

        return tours.map(tour -> TourDetailResponse.builder()
                .id(tour.getId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .startDate(tour.getStartDate())
                .endDate(tour.getEndDate())
                .registrationDeadline(tour.getRegistrationDeadline())
                .maxParticipants(tour.getMaxParticipants())
                .currentParticipants(tour.getCurrentParticipants())
                .pricePerPerson(tour.getPricePerPerson())
                .status(tour.getStatus())
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .itinerary(mapItineraryInfo(tour.getItinerary()))
                .creator(mapUserInfo(tour.getCreator()))
                .build());
    }

    /** Cập nhật tour */
    @Transactional
    public Tour updateTour(String tourId, UpdateTourRequest req, User user) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        // Chỉ creator mới được update
        if (!tour.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("Only creator can update tour");
        }

        // Không cho update nếu tour đã bắt đầu
        if (tour.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot update tour that has already started");
        }

        // Update các field
        if (req.getTitle() != null) {
            tour.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            tour.setDescription(req.getDescription());
        }
        if (req.getStartDate() != null) {
            tour.setStartDate(req.getStartDate());
        }
        if (req.getEndDate() != null) {
            tour.setEndDate(req.getEndDate());
        }
        if (req.getRegistrationDeadline() != null) {
            tour.setRegistrationDeadline(req.getRegistrationDeadline());
        }
        if (req.getMaxParticipants() != null) {
            // Không cho giảm xuống dưới số người đã đăng ký
            if (req.getMaxParticipants() < tour.getCurrentParticipants()) {
                throw new IllegalStateException("Cannot set max participants below current participants");
            }
            tour.setMaxParticipants(req.getMaxParticipants());
        }
        if (req.getPricePerPerson() != null) {
            tour.setPricePerPerson(req.getPricePerPerson());
        }
        if (req.getStatus() != null) {
            tour.setStatus(req.getStatus());
        }

        return tourRepo.save(tour);
    }

    /** Xóa tour */
    @Transactional
    public void deleteTour(String tourId, User user) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        // Chỉ creator mới được xóa
        if (!tour.getCreator().getId().equals(user.getId())) {
            throw new IllegalStateException("Only creator can delete tour");
        }

        // Không cho xóa nếu đã có người đăng ký
        if (tour.getCurrentParticipants() > 0) {
            throw new IllegalStateException("Cannot delete tour with participants");
        }

        // Không cho xóa nếu tour đã bắt đầu
        if (tour.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot delete tour that has already started");
        }

        tourRepo.delete(tour);
    }

    // ... các method mapping và joinTour, cancelJoin giữ nguyên

    private TourDetailResponse.ItineraryInfo mapItineraryInfo(Itinerary itinerary) {
        if (itinerary == null) return null;
        return TourDetailResponse.ItineraryInfo.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .build();
    }

    private TourDetailResponse.UserInfo mapUserInfo(User user) {
        if (user == null) return null;
        return TourDetailResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatar(user.getAvatar())
                .build();
    }

    private TourDetailResponse.ParticipantInfo mapParticipantInfo(TourParticipant participant) {
        return TourDetailResponse.ParticipantInfo.builder()
                .id(participant.getId())
                .user(mapUserInfo(participant.getUser()))
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    @Transactional
    public TourParticipant joinTour(String tourId, User user) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        if (tour.getRegistrationDeadline() != null &&
                tour.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Registration closed");
        }

        if (participantRepo.existsByTourIdAndUserId(tourId, user.getId())) {
            throw new IllegalStateException("User already joined");
        }

        int current = Optional.ofNullable(tour.getCurrentParticipants()).orElse(0);
        if (current >= tour.getMaxParticipants()) {
            throw new IllegalStateException("Tour is full");
        }

        TourParticipant participant = TourParticipant.builder()
                .tour(tour)
                .user(user)
                .build();

        participant = participantRepo.save(participant);

        tour.setCurrentParticipants(current + 1);
        tourRepo.save(tour);

        return participant;
    }

    @Transactional
    public void cancelJoin(String tourId, User user) {
        Tour tour = tourRepo.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        if (tour.getRegistrationDeadline() != null &&
                tour.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel after registration deadline");
        }

        TourParticipant participant = participantRepo.findByTourIdAndUserId(tourId, user.getId())
                .orElseThrow(() -> new IllegalStateException("User not joined this tour"));

        participantRepo.delete(participant);

        int current = Optional.ofNullable(tour.getCurrentParticipants()).orElse(0);
        tour.setCurrentParticipants(Math.max(0, current - 1));
        tourRepo.save(tour);
    }
}