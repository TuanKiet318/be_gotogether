package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final UserRepository userRepo;
    private final UserRepository userRepository;

    // Tạo blog thông thường (từ place)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BlogDetailResponse> createBlog(
            Authentication authentication,
            @Valid @RequestBody BlogCreateRequest req) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        return ResponseEntity.ok(blogService.createBlog(user.getId(), req));
    }

    // Lấy chi tiết blog theo ID
    @GetMapping("/{id}")
    public ResponseEntity<BlogDetailResponse> getBlog(
            @PathVariable String id,
            Authentication authentication) {

        String userId = null;
        if (authentication != null) {
            User user = userRepo.findByEmail(authentication.getName()).orElse(null);
            if (user != null) userId = user.getId();
        }

        return ResponseEntity.ok(blogService.getBlogDetail(userId, id));
    }

    // Lấy blog theo slug (SEO friendly)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogDetailResponse> getBlogBySlug(
            @PathVariable String slug,
            Authentication authentication) {

        String userId = null;
        if (authentication != null) {
            User user = userRepo.findByEmail(authentication.getName()).orElse(null);
            if (user != null) userId = user.getId();
        }

        return ResponseEntity.ok(blogService.getBlogBySlug(userId, slug));
    }

    // Danh sách blog public
    @GetMapping
    public ResponseEntity<Page<BlogSummaryResponse>> listBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = null;

        // Nếu user đã đăng nhập
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {


            userId = userRepository.findByEmail(authentication.getName())
                    .map(User::getId)
                    .orElse(null);    // tránh lỗi khi user bị xóa
        }

        return ResponseEntity.ok(blogService.getPublicBlogs(pageable, userId));
    }



    // Danh sách blog của tôi
    @GetMapping("/my-blogs")
    public ResponseEntity<Page<BlogSummaryResponse>> getMyBlogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(blogService.getUserBlogs(user.getId(), pageable));
    }

    // Cập nhật blog
    @PutMapping("/{id}")
    public ResponseEntity<BlogDetailResponse> updateBlog(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody UpdateBlogRequest req) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        return ResponseEntity.ok(blogService.updateBlog(user.getId(), id, req));
    }

    // Publish blog
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishBlog(
            @PathVariable String id,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        blogService.publishBlog(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Blog đã được publish"));
    }

    // Xóa blog
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(
            @PathVariable String id,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        blogService.deleteBlog(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // Thêm comment
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String id,
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        blogService.addComment(id, user.getId(), body.get("comment"));
        return ResponseEntity.ok(Map.of("message", "Comment đã được thêm"));
    }

    // Like/Unlike
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable String id,
            Authentication authentication) {

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        boolean liked = blogService.toggleLike(id, user.getId());
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}