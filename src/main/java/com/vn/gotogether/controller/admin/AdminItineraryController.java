package com.vn.gotogether.controller.admin;

import com.vn.gotogether.dto.data.FeatureItineraryRequest;
import com.vn.gotogether.dto.data.ItineraryFeaturedDetailResponse;
import com.vn.gotogether.dto.data.ItinerarySummaryResponse;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.ItineraryRepository;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.ItineraryService;
import com.vn.gotogether.service.data.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.vn.gotogether.dto.data.AdminCloneAndFeatureRequest;

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
    private final ItineraryRepository itineraryRepo;

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


    @GetMapping("/search")
    public Page<ItinerarySummaryResponse> searchItinerariesForAdmin(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Cần thêm hàm findByTitleContainingIgnoreCase... vào ItineraryRepository
        return itineraryRepo.findByTitleContainingIgnoreCaseOrUser_NameContainingIgnoreCase(
                keyword, keyword, PageRequest.of(page, size)
        ).map(i -> ItinerarySummaryResponse.builder()
                .id(i.getId())
                .title(i.getTitle())
                .ownerName(i.getUser() != null ? i.getUser().getName() : "Unknown")
                .build());
    }

    // TÍNH NĂNG 2: API MỚI CHO VIỆC CLONE & VÀ ĐÁNH DẤU NỔI BẬT
    @PostMapping(
            value = "/clone-and-feature",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryFeaturedDetailResponse cloneAndFeatureItinerary(
            @Valid @ModelAttribute AdminCloneAndFeatureRequest request,
            Authentication authentication
    ) {
        User admin = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        // Upload ảnh bìa mới
        Set<String> heroImageUrls = uploadHeroImages(request.getHeroImageFiles(), admin);

        // Gọi Service gộp 2 thao tác
        return itineraryService.cloneAndFeatureByAdmin(
                admin.getId(),
                request.getSourceItineraryId(),
                request.getOverview(),
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
