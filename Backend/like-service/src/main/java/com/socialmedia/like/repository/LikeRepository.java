package com.socialmedia.like.repository;

import com.socialmedia.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT l FROM Like l WHERE l.postId = :postId AND l.userId = :userId")
    Optional<Like> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);

    @Query("SELECT l.postId, COUNT(l) FROM Like l WHERE l.postId IN :postIds GROUP BY l.postId")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}
