package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.CreateTourRequest;
import com.vn.gotogether.dto.data.TourDetailResponse;
import com.vn.gotogether.dto.data.UpdateTourRequest;
import com.vn.gotogether.entity.Tour;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.model.TourStatus;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.TourService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final UserRepository userRepo;

    /**
     * Lấy danh sách tours với phân trang và filter
     * GET /api/tours?page=0&size=10&status=UPCOMING&creatorId=xxx&startDateFrom=2025-11-01&startDateTo=2025-12-31
     */
    @GetMapping
    public ResponseEntity<Page<TourDetailResponse>> listTours(
            @RequestParam(required = false) TourStatus status,
            @RequestParam(required = false) String creatorId,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TourDetailResponse> tours = tourService.listTours(
                status, creatorId, startDateFrom, startDateTo, pageable);

        return ResponseEntity.ok(tours);
    }

    /**
     * Lấy chi tiết tour
     * GET /api/tours/{tourId}
     */
    @GetMapping("/{tourId}")
    public ResponseEntity<TourDetailResponse> getTourDetail(@PathVariable String tourId) {
        TourDetailResponse response = tourService.getTourDetail(tourId);
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo tour mới
     * POST /api/tours
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTour(
            @RequestBody CreateTourRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        String creatorId = jwt.getClaimAsString("id");

        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tour tour = tourService.createTour(req, creator);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tạo tour thành công");
        response.put("tourId", tour.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật tour
     * PUT /api/tours/{tourId}
     */
    @PutMapping("/{tourId}")
    public ResponseEntity<Map<String, Object>> updateTour(
            @PathVariable String tourId,
            @RequestBody UpdateTourRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tour tour = tourService.updateTour(tourId, req, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cập nhật tour thành công");
        response.put("tourId", tour.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa tour
     * DELETE /api/tours/{tourId}
     */
    @DeleteMapping("/{tourId}")
    public ResponseEntity<Map<String, Object>> deleteTour(
            @PathVariable String tourId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tourService.deleteTour(tourId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Xóa tour thành công");
        response.put("tourId", tourId);

        return ResponseEntity.ok(response);
    }

    /**
     * Tham gia tour
     * POST /api/tours/{tourId}/join
     */
    @PostMapping("/{tourId}/join")
    public ResponseEntity<Map<String, Object>> joinTour(
            @PathVariable String tourId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tourService.joinTour(tourId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tham gia tour thành công");
        response.put("tourId", tourId);

        return ResponseEntity.ok(response);
    }

    /**
     * Hủy tham gia tour
     * DELETE /api/tours/{tourId}/cancel
     */
    @DeleteMapping("/{tourId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelJoin(
            @PathVariable String tourId,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tourService.cancelJoin(tourId, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hủy tham gia tour thành công");
        response.put("tourId", tourId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/joined")
    public ResponseEntity<List<TourDetailResponse>> getJoinedTours(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("id");
        List<TourDetailResponse> tours = tourService.getJoinedTours(userId);
        return ResponseEntity.ok(tours);
    }
}