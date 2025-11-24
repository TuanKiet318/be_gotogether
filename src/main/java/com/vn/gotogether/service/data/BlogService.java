package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.*;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.*;
import com.vn.gotogether.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepo;
    private final BlogMediaRepository mediaRepo;
    private final BlogCommentRepository commentRepo;
    private final BlogLikeRepository likeRepo;
    private final UserRepository userRepo;
    private final PlaceRepository placeRepo;
    private final ItineraryRepository itineraryRepo;
    private final ItineraryBlogRepository itineraryBlogRepo;
    private final ItineraryMediaRepository itineraryMediaRepo;
    private final PermissionService permissionService;

    // ===== TẠO BLOG TỪ ITINERARY =====
    @Transactional
    public BlogDetailResponse createBlogFromItinerary(String userId, String itineraryId, CreateBlogFromItineraryRequest req) {

        // Kiểm tra quyền xem itinerary
        if (!permissionService.canView(itineraryId, userId)) {
            throw new AccessDeniedException("Bạn không có quyền tạo blog từ lịch trình này");
        }

        Itinerary itinerary = itineraryRepo.findById(itineraryId)
                .orElseThrow(() -> new InvalidDataException("Lịch trình không tồn tại"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        // Tạo slug từ title
        String slug = generateSlug(req.getTitle());

        // Tạo blog
        Blog blog = Blog.builder()
                .user(user)
                .title(req.getTitle())
                .slug(slug)
                .content(req.getContent())
                .excerpt(req.getExcerpt())
                .isPublic(true)
                .status(req.getAutoPublish() ? Blog.Status.PUBLISHED : Blog.Status.DRAFT)
                .build();

        blog = blogRepo.save(blog);

        // Liên kết itinerary với blog
        ItineraryBlog itineraryBlog = ItineraryBlog.builder()
                .itinerary(itinerary)
                .blog(blog)
                .build();
        itineraryBlogRepo.save(itineraryBlog);

        // Copy media từ itinerary (nếu được yêu cầu)
        if (req.getIncludeMedia()) {
            List<ItineraryMedia> itineraryMedia = itineraryMediaRepo
                    .findByItinerary_IdOrderByDayNumberAscOrderInDayAsc(itineraryId);

            for (ItineraryMedia im : itineraryMedia) {
                BlogMedia.MediaType mediaType = im.getMediaType() == ItineraryMedia.MediaType.IMAGE
                        ? BlogMedia.MediaType.IMAGE
                        : BlogMedia.MediaType.VIDEO;

                BlogMedia bm = BlogMedia.builder()
                        .blog(blog)
                        .mediaUrl(im.getMediaUrl())
                        .mediaType(mediaType)
                        .description(im.getCaption())
                        .build();
                mediaRepo.save(bm);
            }
        }

        return getBlogDetail(userId, blog.getId());
    }

    // ===== TẠO BLOG THÔNG THƯỜNG (như cũ) =====
    @Transactional
    public BlogDetailResponse createBlog(String userId, BlogCreateRequest req) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        Place place = req.getPlaceId() != null
                ? placeRepo.findById(req.getPlaceId()).orElse(null)
                : null;

        String slug = generateSlug(req.getTitle() != null ? req.getTitle() : "Untitled");

        Blog blog = Blog.builder()
                .user(user)
                .place(place)
                .title(req.getTitle())
                .slug(slug)
                .content(req.getContent())
                .isPublic(true)
                .status(Blog.Status.DRAFT)
                .build();

        blog = blogRepo.save(blog);

        // Lưu media
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

        return getBlogDetail(userId, blog.getId());
    }

    // ===== LẤY CHI TIẾT BLOG =====
    @Transactional
    public BlogDetailResponse getBlogDetail(String currentUserId, String blogId) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));

        // Tăng view count
        blogRepo.incrementViewCount(blogId);

        // Lấy media
        List<MediaDto> media = mediaRepo.findByBlogId(blog.getId())
                .stream().map(m -> MediaDto.builder()
                        .url(m.getMediaUrl())
                        .type(m.getMediaType().name())
                        .description(m.getDescription())
                        .build()).collect(Collectors.toList());

        // Lấy comments
        List<BlogDetailResponse.CommentDto> comments = commentRepo.findByBlogIdOrderByCreatedAtDesc(blog.getId())
                .stream().map(c -> BlogDetailResponse.CommentDto.builder()
                        .id(c.getId())
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getName())
                        .userAvatar(c.getUser().getAvatar())
                        .comment(c.getComment())
                        .createdAt(c.getCreatedAt())
                        .build()).collect(Collectors.toList());

        long likeCount = likeRepo.countByBlogId(blog.getId());
        boolean isLiked = currentUserId != null && likeRepo.existsByBlogIdAndUserId(blog.getId(), currentUserId);

        // Kiểm tra liên kết với itinerary
        String itineraryId = null;
        String itineraryTitle = null;
        var itineraryBlogOpt = itineraryBlogRepo.findByItinerary_IdAndBlog_Id(null, blog.getId());
        if (itineraryBlogOpt.isPresent()) {
            Itinerary it = itineraryBlogOpt.get().getItinerary();
            itineraryId = it.getId();
            itineraryTitle = it.getTitle();
        }

        return BlogDetailResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .content(blog.getContent())
                .excerpt(blog.getExcerpt())
                .viewCount(blog.getViewCount())
                .status(blog.getStatus().name())
                .isPublic(blog.getIsPublic())
                .authorId(blog.getUser().getId())
                .authorName(blog.getUser().getName())
                .authorAvatar(blog.getUser().getAvatar())
                .media(media)
                .likeCount(likeCount)
                .commentCount((long) comments.size())
                .isLiked(isLiked)
                .itineraryId(itineraryId)
                .itineraryTitle(itineraryTitle)
                .comments(comments)
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    // ===== LẤY BLOG THEO SLUG =====
    @Transactional
    public BlogDetailResponse getBlogBySlug(String currentUserId, String slug) {
        Blog blog = blogRepo.findBySlug(slug)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));

        return getBlogDetail(currentUserId, blog.getId());
    }

    // ===== DANH SÁCH BLOG (PUBLIC) =====
    public Page<BlogSummaryResponse> getPublicBlogs(Pageable pageable, String userId) {
        return blogRepo.findAllByIsPublicTrueAndStatus(Blog.Status.PUBLISHED, pageable)
                .map(blog -> mapToSummary(blog, userId));
    }


    // ===== DANH SÁCH BLOG CỦA USER =====
    public Page<BlogSummaryResponse> getUserBlogs(String userId, Pageable pageable) {
        return blogRepo.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(blog -> mapToSummary(blog, userId));
    }

    // ===== CẬP NHẬT BLOG =====
    @Transactional
    public BlogDetailResponse updateBlog(String userId, String blogId, UpdateBlogRequest req) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));

        if (!blog.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa blog này");
        }

        if (req.getTitle() != null) {
            blog.setTitle(req.getTitle());
            blog.setSlug(generateSlug(req.getTitle()));
        }
        if (req.getContent() != null) {
            blog.setContent(req.getContent());
        }
        if (req.getExcerpt() != null) {
            blog.setExcerpt(req.getExcerpt());
        }
        if (req.getIsPublic() != null) {
            blog.setIsPublic(req.getIsPublic());
        }

        blog = blogRepo.save(blog);
        return getBlogDetail(userId, blog.getId());
    }

    // ===== PUBLISH BLOG =====
    @Transactional
    public void publishBlog(String userId, String blogId) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));

        if (!blog.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền publish blog này");
        }

        blog.setStatus(Blog.Status.PUBLISHED);
        blogRepo.save(blog);
    }

    // ===== XÓA BLOG =====
    @Transactional
    public void deleteBlog(String blogId, String userId) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));

        if (!blog.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa blog này");
        }

        blogRepo.delete(blog);
    }

    // ===== THÊM COMMENT =====
    @Transactional
    public void addComment(String blogId, String userId, String comment) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        commentRepo.save(BlogComment.builder()
                .blog(blog)
                .user(user)
                .comment(comment)
                .build());
    }

    // ===== LIKE / UNLIKE =====
    @Transactional
    public boolean toggleLike(String blogId, String userId) {
        boolean liked = likeRepo.existsByBlogIdAndUserId(blogId, userId);

        if (liked) {
            likeRepo.deleteByBlogIdAndUserId(blogId, userId);
            return false;
        }

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new InvalidDataException("Blog không tồn tại"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User không tồn tại"));

        likeRepo.save(BlogLike.builder()
                .blog(blog)
                .user(user)
                .build());

        return true;
    }

    // ===== LẤY BLOG TỪ ITINERARY =====
    public List<BlogSummaryResponse> getBlogsByItinerary(String itineraryId) {

        return itineraryBlogRepo.findByItinerary_Id(itineraryId)
                .stream()
                .map(ib -> mapToSummary(ib.getBlog(), null))
                .collect(Collectors.toList());
    }

    // ===== HELPERS =====
    private String generateSlug(String title) {
        String slug = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");

        // Nếu slug đã tồn tại, thêm UUID
        if (blogRepo.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        return slug;
    }

    private BlogSummaryResponse mapToSummary(Blog blog, String userId) {
        // Lấy ảnh đầu tiên
        String featuredImage = null;
        if (!blog.getMedia().isEmpty()) {
            featuredImage = blog.getMedia().get(0).getMediaUrl();
        }
        // Đếm tim
        long likeCount = likeRepo.countByBlogId(blog.getId());
        // Đếm comment
        long commentCount = commentRepo.countByBlogId(blog.getId());
        // Kiểm tra user đã like chưa
        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeRepo.existsByBlogIdAndUserId(blog.getId(), userId);
        }
        List<MediaDto> media = mediaRepo.findByBlogId(blog.getId())
                .stream().map(m -> MediaDto.builder()
                        .url(m.getMediaUrl())
                        .type(m.getMediaType().name())
                        .description(m.getDescription())
                        .build()).collect(Collectors.toList());
        return BlogSummaryResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .excerpt(blog.getExcerpt())
                .viewCount(blog.getViewCount())
                .status(blog.getStatus().name())
                .authorName(blog.getUser().getName())
                .authorAvatar(blog.getUser().getAvatar())
                .media(media)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .createdAt(blog.getCreatedAt())
                .build();
    }

}