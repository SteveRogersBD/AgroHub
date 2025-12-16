package com.example.agrohub.domain.model

/**
 * Domain model representing follow statistics for a user.
 *
 * @property followersCount Number of users following this user
 * @property followingCount Number of users this user is following
 */
data class FollowStats(
    val followersCount: Int,
    val followingCount: Int
)
