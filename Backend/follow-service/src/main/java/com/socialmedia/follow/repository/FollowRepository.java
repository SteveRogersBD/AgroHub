package com.socialmedia.follow.repository;

import com.socialmedia.follow.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :userId")
    Page<Long> findFollowerIdsByFollowingId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    Page<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId, Pageable pageable);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);
}
