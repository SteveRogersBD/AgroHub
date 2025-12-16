package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for user profile operations.
 * Handles profile creation, updates, retrieval, and search.
 */
interface UserRepository {
    /**
     * Creates a new user profile.
     *
     * @param name User's display name (optional)
     * @param bio User's biography (optional)
     * @param avatarUrl URL to user's avatar image (optional)
     * @param location User's location (optional)
     * @param website User's website URL (optional)
     * @return Result containing the created User on success, or an error
     */
    suspend fun createProfile(
        name: String?,
        bio: String?,
        avatarUrl: String?,
        location: String?,
        website: String?
    ): Result<User>
    
    /**
     * Updates an existing user profile.
     *
     * @param userId ID of the user to update
     * @param name User's display name (optional)
     * @param bio User's biography (optional)
     * @param avatarUrl URL to user's avatar image (optional)
     * @param location User's location (optional)
     * @param website User's website URL (optional)
     * @return Result containing the updated User on success, or an error
     */
    suspend fun updateProfile(
        userId: Long,
        name: String?,
        bio: String?,
        avatarUrl: String?,
        location: String?,
        website: String?
    ): Result<User>
    
    /**
     * Retrieves the current authenticated user's profile.
     *
     * @return Result containing the current User on success, or an error
     */
    suspend fun getCurrentUser(): Result<User>
    
    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId ID of the user to retrieve
     * @return Result containing the User on success, or an error
     */
    suspend fun getUserById(userId: Long): Result<User>
    
    /**
     * Retrieves a user profile by username.
     *
     * @param username Username of the user to retrieve
     * @return Result containing the User on success, or an error
     */
    suspend fun getUserByUsername(username: String): Result<User>
    
    /**
     * Searches for users matching the query string.
     *
     * @param query Search query string
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Users on success, or an error
     */
    suspend fun searchUsers(query: String, page: Int, size: Int): Result<PagedData<User>>
}
