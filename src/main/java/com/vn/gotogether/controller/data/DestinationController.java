package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.DestinationResponseDto;
import com.vn.gotogether.service.data.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
@CrossOrigin
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public ResponseEntity<List<DestinationResponseDto>> getAllDestinations() {
        List<DestinationResponseDto> destinations = destinationService.getAllDestinations();
        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<DestinationResponseDto>> getDestinationsByCountry(
            @PathVariable String country) {
        List<DestinationResponseDto> destinations = destinationService.getDestinationsByCountry(country);
        return ResponseEntity.ok(destinations);
    }
}
