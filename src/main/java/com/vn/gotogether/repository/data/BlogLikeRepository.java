package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, String> {
    boolean existsByBlogIdAndUserId(String blogId, String userId);
    long countByBlogId(String blogId);
    void deleteByBlogIdAndUserId(String blogId, String userId);
    void deleteAllByBlogId(String blogId);
}