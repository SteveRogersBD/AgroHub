package com.example.agrohub.domain.repository

import com.example.agrohub.domain.model.PagedData
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.util.Result

/**
 * Repository interface for post operations.
 * Handles post creation, updates, deletion, and retrieval.
 */
interface PostRepository {
    /**
     * Creates a new post.
     *
     * @param content The text content of the post
     * @param mediaUrl URL to attached media (optional)
     * @return Result containing the created Post on success, or an error
     */
    suspend fun createPost(content: String, mediaUrl: String?): Result<Post>
    
    /**
     * Updates an existing post.
     *
     * @param postId ID of the post to update
     * @param content The updated text content
     * @param mediaUrl URL to attached media (optional)
     * @return Result containing the updated Post on success, or an error
     */
    suspend fun updatePost(postId: Long, content: String, mediaUrl: String?): Result<Post>
    
    /**
     * Deletes a post (soft delete).
     *
     * @param postId ID of the post to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePost(postId: Long): Result<Unit>
    
    /**
     * Retrieves a post by its ID.
     *
     * @param postId ID of the post to retrieve
     * @return Result containing the Post on success, or an error
     */
    suspend fun getPostById(postId: Long): Result<Post>
    
    /**
     * Retrieves a paginated list of posts by a specific user.
     *
     * @param userId ID of the user whose posts to retrieve
     * @param page Page number (0-indexed)
     * @param size Number of items per page
     * @return Result containing PagedData of Posts on success, or an error
     */
    suspend fun getUserPosts(userId: Long, page: Int, size: Int): Result<PagedData<Post>>
}
