package com.socialmedia.comment.repository;

import com.socialmedia.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId, Pageable pageable);
}
