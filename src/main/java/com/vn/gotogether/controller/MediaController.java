package com.vn.gotogether.controller;

import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService uploadService;
    private final UserRepository userRepo;

    @PostMapping("/itinerary")
    public ResponseEntity<?> uploadItineraryMedia(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        String url = uploadService.uploadMedia(file, user.getId(), "itinerary");

        // Nếu là video, tạo thumbnail
        String thumbnail = null;
        if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            thumbnail = uploadService.generateThumbnail(url);
        }

        return ResponseEntity.ok(Map.of(
                "url", url,
                "thumbnail", thumbnail != null ? thumbnail : "",
                "type", file.getContentType().startsWith("image/") ? "IMAGE" : "VIDEO"
        ));
    }

    @PostMapping("/blog")
    public ResponseEntity<?> uploadBlogMedia(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        String url = uploadService.uploadMedia(file, user.getId(), "blog");

        String thumbnail = null;
        if (file.getContentType() != null && file.getContentType().startsWith("video/")) {
            thumbnail = uploadService.generateThumbnail(url);
        }

        return ResponseEntity.ok(Map.of(
                "url", url,
                "thumbnail", thumbnail != null ? thumbnail : "",
                "type", file.getContentType().startsWith("image/") ? "IMAGE" : "VIDEO"
        ));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        // Validate chỉ cho phép image
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InvalidDataException("Avatar phải là file ảnh");
        }

        // Xóa avatar cũ nếu tồn tại
        if (user.getAvatar() != null && !user.getAvatar().isBlank()) {
            uploadService.deleteMedia(user.getAvatar());
        }

        // Upload avatar (folder riêng)
        String avatarUrl = uploadService.uploadMedia(file, user.getId(), "avatar");

        // Lưu avatar vào user
        user.setAvatar(avatarUrl);
        userRepo.save(user);

        return ResponseEntity.ok(Map.of(
                "avatar", avatarUrl
        ));
    }

}