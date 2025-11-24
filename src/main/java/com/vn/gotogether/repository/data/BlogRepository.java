package com.vn.gotogether.repository.data;

import com.vn.gotogether.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String> {

    @EntityGraph(attributePaths = {
            "media",
            "user"
    })
    Page<Blog> findAllByIsPublicTrueAndStatus(Blog.Status status, Pageable pageable);


    Optional<Blog> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Blog> findByUser_IdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    void incrementViewCount(String blogId);
}