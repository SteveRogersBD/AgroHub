package com.socialmedia.like.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_likes_post_user", columnNames = {"post_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_likes_post_id", columnList = "post_id"),
        @Index(name = "idx_likes_user_id", columnList = "user_id"),
        @Index(name = "idx_likes_composite", columnList = "post_id, user_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
