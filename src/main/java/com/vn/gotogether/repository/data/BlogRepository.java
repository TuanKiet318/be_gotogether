package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, String> {
    Page<Blog> findAllByIsPublicTrue(Pageable pageable);
}

