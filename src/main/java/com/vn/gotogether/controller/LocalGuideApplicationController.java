package com.vn.gotogether.controller;

import com.vn.gotogether.dto.localguide.LocalGuideApplicationCreateRequest;
import com.vn.gotogether.dto.localguide.LocalGuideApplicationResponse;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.enums.ApplicationStatus;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.LocalGuideApplicationService;
import com.vn.gotogether.service.data.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/local-guides")
@RequiredArgsConstructor
public class LocalGuideApplicationController {

    private final LocalGuideApplicationService service;
    private final MediaService uploadService;
    private final UserRepository userRepo;

    // ----------------------------------------------------
    //  CREATE APPLICATION (USER)
    // ----------------------------------------------------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LocalGuideApplicationResponse apply(
            @Valid @ModelAttribute LocalGuideApplicationCreateRequest request,
            Authentication authentication
    ) {
        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        String frontUrl = upload(request.getFrontImageFile(), user);
        String backUrl = upload(request.getBackImageFile(), user);
        String selfieUrl = upload(request.getSelfieWithIdFile(), user);

        String portfolioUrl = uploadOptional(request.getPortfolioFile(), user);
        String certificateUrl = uploadOptional(request.getCertificateFile(), user);

        return service.apply(
                request,
                frontUrl,
                backUrl,
                selfieUrl,
                portfolioUrl,
                certificateUrl,
                user
        );
    }

    private String upload(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDataException("Thiếu file bắt buộc");
        }
        return uploadService.uploadMedia(file, user.getId(), "guide");
    }

    private String uploadOptional(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) return null;
        return uploadService.uploadMedia(file, user.getId(), "guide");
    }



    // ----------------------------------------------------
    //  GET DETAILS BY ID
    // ----------------------------------------------------
    @GetMapping("/me")
    public LocalGuideApplicationResponse get() {
        return service.getById();
    }

    // ----------------------------------------------------
    //  LIST FILTER BY STATUS (ADMIN)
    // ----------------------------------------------------
    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LocalGuideApplicationResponse> listByStatus(
            @RequestParam(required = false) ApplicationStatus status
    ) {
        return service.getList(status);
    }

    // ----------------------------------------------------
    //  ADMIN – VERIFY / APPROVE / REJECT
    // ----------------------------------------------------
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public LocalGuideApplicationResponse verify(
            @PathVariable String id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reviewerNote
    ) {
        return service.verify(id, approved, reviewerNote);
    }

    // ----------------------------------------------------
    //  USER – UPDATE APPLICATION BEFORE APPROVED
    // ----------------------------------------------------
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LocalGuideApplicationResponse update(
            @PathVariable String id,
            @ModelAttribute LocalGuideApplicationCreateRequest request,
            Authentication authentication
    ) {
        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        return service.updateApplication(id, request, user);
    }

    // ----------------------------------------------------
    //  DELETE APPLICATION (ADMIN ONLY)
    // ----------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    // ----------------------------------------------------
    //  ADD REVIEW RATING (WEBHOOK OR INTERNAL)
    // ----------------------------------------------------
    @PostMapping("/{id}/review")
    public LocalGuideApplicationResponse addReview(
            @PathVariable String id,
            @RequestParam int rating
    ) {
        return service.addRating(id, rating);
    }
}
