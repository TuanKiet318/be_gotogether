package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryDayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itineraries/{itineraryId}/days")
@RequiredArgsConstructor
public class ItineraryDayController {

    private final ItineraryDayService dayService;
    private final UserRepository userRepository;

    @PostMapping("/insert-before")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryDayResponse insertBefore(
            @PathVariable String itineraryId,
            @Valid @RequestBody InsertDayRequest req) {
        String userId = currentUserId();
        return dayService.insertBefore(userId, itineraryId, req.getDayNumber(), req.getCount());
    }

    @PostMapping("/insert-after")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryDayResponse insertAfter(
            @PathVariable String itineraryId,
            @Valid @RequestBody InsertDayRequest req) {
        String userId = currentUserId();
        return dayService.insertAfter(userId, itineraryId, req.getDayNumber(), req.getCount());
    }

    @DeleteMapping("/{dayNumber}")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryDayResponse removeDay(
            @PathVariable String itineraryId,
            @PathVariable int dayNumber) {
        String userId = currentUserId();
        return dayService.removeDay(userId, itineraryId, dayNumber);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        return user.getId();
    }


}
