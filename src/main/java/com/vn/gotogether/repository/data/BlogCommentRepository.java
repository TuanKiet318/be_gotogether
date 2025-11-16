package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogCommentRepository extends JpaRepository<BlogComment, String> {
    List<BlogComment> findByBlogIdOrderByCreatedAtDesc(String blogId);
}
