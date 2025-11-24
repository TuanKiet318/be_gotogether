package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.BlogMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogMediaRepository extends JpaRepository<BlogMedia, String> {
    List<BlogMedia> findByBlogId(String blogId);
    void deleteAllByBlogId(String blogId);
}