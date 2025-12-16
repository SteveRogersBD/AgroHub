package com.socialmedia.user.repository;

import com.socialmedia.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    @Query("SELECT up FROM UserProfile up WHERE LOWER(up.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<UserProfile> searchByName(@Param("name") String name, Pageable pageable);

    boolean existsByUserId(Long userId);
}
