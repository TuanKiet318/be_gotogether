package com.vn.gotogether.controller.data;

import com.vn.gotogether.dto.data.BlogCreateRequest;
import com.vn.gotogether.service.data.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping
    public ResponseEntity<?> createBlog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody BlogCreateRequest req) {

        String userId = jwt.getClaimAsString("id");
        return ResponseEntity.ok(blogService.createBlog(userId, req));
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getBlog(@PathVariable String id) {
        return ResponseEntity.ok(blogService.getBlog(id));
    }

    @GetMapping
    public ResponseEntity<?> listBlogs(@RequestParam int page,
                                       @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(blogService.getBlogs(pageable));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body) {

        String userId = jwt.getClaimAsString("id");

        blogService.addComment(id, userId, body.get("comment"));
        return ResponseEntity.ok("Comment added");
    }


    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");
        boolean liked = blogService.toggleLike(id, userId);

        return ResponseEntity.ok(Map.of("liked", liked));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");

        blogService.deleteBlog(id, userId);
        return ResponseEntity.ok("Deleted");
    }

}

