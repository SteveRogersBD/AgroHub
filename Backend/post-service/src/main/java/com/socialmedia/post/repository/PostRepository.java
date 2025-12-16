package com.socialmedia.post.repository;

import com.socialmedia.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);
}
