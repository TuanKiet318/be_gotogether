package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.CreateItineraryRequest;
import com.vn.gotogether.dto.data.ItineraryResponse;
import com.vn.gotogether.service.data.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;

    @PostMapping
    public ResponseEntity<ItineraryResponse> create(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody CreateItineraryRequest req
    ) {
        String id = itineraryService.createItinerary(userId, req);
        return ResponseEntity.ok(ItineraryResponse.builder().id(id).build());
    }
}
