package com.vn.gotogether.controller.admin;

import com.vn.gotogether.dto.data.FeatureItineraryRequest;
import com.vn.gotogether.dto.data.ItineraryFeaturedDetailResponse;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryService;
import com.vn.gotogether.service.data.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/itineraries")
@RequiredArgsConstructor
public class AdminItineraryController {

    private final ItineraryService itineraryService;
    private final MediaService uploadService;
    private final UserRepository userRepo;

    @PutMapping(
            value = "/{id}/feature",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ItineraryFeaturedDetailResponse featureItinerary(
            @PathVariable String id,
            @Valid @ModelAttribute FeatureItineraryRequest request,
            Authentication authentication
    ) {
        User admin = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        Set<String> heroImageUrls = uploadHeroImages(
                request.getHeroImageFiles(),
                admin
        );

        return itineraryService.featureItinerary(
                id,
                request,
                heroImageUrls
        );
    }

    /* ===================== PRIVATE UPLOAD ===================== */

    private Set<String> uploadHeroImages(
            List<MultipartFile> files,
            User user
    ) {
        if (files == null || files.isEmpty()) {
            throw new InvalidDataException("Thiếu hero image");
        }

        return files.stream()
                .filter(f -> !f.isEmpty())
                .map(f -> uploadService.uploadMedia(
                        f,
                        user.getId(),
                        "itinerary/featured"
                ))
                .collect(Collectors.toSet());
    }
}
