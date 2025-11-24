package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, String> {
    List<BlogComment> findByBlogIdOrderByCreatedAtDesc(String blogId);
    long countByBlogId(String blogId);
    void deleteAllByBlogId(String blogId);
}