package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogMediaRepository extends JpaRepository<BlogMedia, String> {
    List<BlogMedia> findByBlogId(String blogId);
}
