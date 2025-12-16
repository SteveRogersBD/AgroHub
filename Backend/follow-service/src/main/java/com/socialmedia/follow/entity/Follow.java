package com.socialmedia.follow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}),
       indexes = {
           @Index(name = "idx_follows_follower_id", columnList = "follower_id"),
           @Index(name = "idx_follows_following_id", columnList = "following_id"),
           @Index(name = "idx_follows_composite", columnList = "follower_id, following_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
