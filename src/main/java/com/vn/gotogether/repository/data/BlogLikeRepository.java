package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogLikeRepository extends JpaRepository<BlogLike, String> {
    boolean existsByBlogIdAndUserId(String blogId, String userId);
    void deleteByBlogIdAndUserId(String blogId, String userId);
    long countByBlogId(String blogId);
}
