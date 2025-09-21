package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.CreateItineraryRequest;
import com.vn.gotogether.dto.data.ItineraryDetailResponse;
import com.vn.gotogether.dto.data.ItineraryResponse;
import com.vn.gotogether.dto.data.ItinerarySummaryResponse;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.ItineraryItemRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final ItineraryItemRepository itemRepo;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryResponse create(
                                    @Valid @RequestBody CreateItineraryRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        String id = itineraryService.createItinerary(user.getId(), req);
        return ItineraryResponse.builder().id(id).build();
    }

    // ====== GET LIST: các lịch trình của tôi ======
    @GetMapping
    public List<ItinerarySummaryResponse> listMine() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        return itineraryService.listByUser(user.getId());
    }

    // ====== GET DETAIL: chi tiết 1 lịch trình (kèm items) ======
    @GetMapping("/{id}")
    public ItineraryDetailResponse getOne(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));

        return itineraryService.getItineraryDetail(user.getId(), id);
    }

}
