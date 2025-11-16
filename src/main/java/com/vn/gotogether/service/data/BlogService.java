package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.BlogCreateRequest;
import com.vn.gotogether.dto.data.BlogResponse;
import com.vn.gotogether.dto.data.MediaCreateRequest;
import com.vn.gotogether.dto.data.MediaDto;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.repository.data.*;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepo;
    private final BlogMediaRepository mediaRepo;
    private final BlogCommentRepository commentRepo;
    private final BlogLikeRepository likeRepo;
    private final UserRepository userRepo;
    private final PlaceRepository placeRepo;

    // CREATE BLOG
    public BlogResponse createBlog(String userId, BlogCreateRequest req) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Place place = placeRepo.findById(req.getPlaceId()).orElse(null);

        Blog blog = Blog.builder()
                .user(user)
                .place(place)
                .content(req.getContent())
                .isPublic(true)
                .build();

        blog = blogRepo.save(blog);

        // SAVE MEDIA
        if (req.getMedia() != null) {
            for (MediaCreateRequest m : req.getMedia()) {
                mediaRepo.save(BlogMedia.builder()
                        .blog(blog)
                        .mediaUrl(m.getUrl())
                        .mediaType(BlogMedia.MediaType.valueOf(m.getType()))
                        .description(m.getDescription())
                        .build());
            }
        }

        return mapToResponse(blog);
    }

    // GET ONE BLOG
    public BlogResponse getBlog(String id) {
        Blog blog = blogRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        return mapToResponse(blog);
    }

    // GET BLOG LIST (Lazy Loading)
    public Page<BlogResponse> getBlogs(Pageable pageable) {
        return blogRepo.findAllByIsPublicTrue(pageable)
                .map(this::mapToResponse);
    }

    // ADD COMMENT
    public void addComment(String blogId, String userId, String comment) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        commentRepo.save(BlogComment.builder()
                .blog(blog)
                .user(user)
                .comment(comment)
                .build());
    }

    // LIKE / UNLIKE
    @Transactional
    public boolean toggleLike(String blogId, String userId) {
        boolean liked = likeRepo.existsByBlogIdAndUserId(blogId, userId);

        if (liked) {
            likeRepo.deleteByBlogIdAndUserId(blogId, userId);
            return false;
        }

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        likeRepo.save(BlogLike.builder()
                .blog(blog)
                .user(user)
                .build());

        return true;
    }

    // DELETE BLOG
    public void deleteBlog(String blogId, String userId) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot delete this blog");
        }

        blogRepo.delete(blog);
    }

    // MAP ENTITY -> DTO
    private BlogResponse mapToResponse(Blog blog) {
        List<MediaDto> media = mediaRepo.findByBlogId(blog.getId())
                .stream().map(m -> MediaDto.builder()
                        .url(m.getMediaUrl())
                        .type(m.getMediaType().name())
                        .description(m.getDescription())
                        .build()).toList();

        long likeCount = likeRepo.countByBlogId(blog.getId());
        long commentCount = commentRepo.count();

        return BlogResponse.builder()
                .id(blog.getId())
                .content(blog.getContent())
                .userName(blog.getUser().getName())
                .userAvatar(blog.getUser().getAvatar())
                .media(media)
                .likes(likeCount)
                .comments(commentCount)
                .createdAt(blog.getCreatedAt())
                .build();
    }
}
