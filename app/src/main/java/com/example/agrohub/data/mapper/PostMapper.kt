package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.FeedPostDto
import com.example.agrohub.data.remote.dto.PostDto
import com.example.agrohub.domain.model.FeedPost
import com.example.agrohub.domain.model.Post
import com.example.agrohub.domain.model.PostAuthor

/**
 * Mapper for converting Post DTOs to domain models.
 * Handles conversion from network layer representations to UI-friendly domain models.
 */
object PostMapper {
    
    /**
     * Converts a PostDto to a Post domain model.
     *
     * @param dto The PostDto from the API
     * @return Post domain model
     * @throws Exception if required fields are missing or date parsing fails
     */
    fun toDomain(dto: PostDto): Post {
        return Post(
            id = dto.id,
            userId = dto.userId,
            content = dto.content ?: "",
            mediaUrl = dto.mediaUrl,
            createdAt = DateTimeUtils.parseDateTime(dto.createdAt),
            updatedAt = DateTimeUtils.parseDateTimeOrNull(dto.updatedAt)
        )
    }
    
    /**
     * Converts a FeedPostDto to a FeedPost domain model.
     * This includes enriched metadata like author information, like counts, and comment counts.
     *
     * @param dto The FeedPostDto from the API
     * @return FeedPost domain model
     * @throws Exception if required fields are missing or date parsing fails
     */
    fun feedPostToDomain(dto: FeedPostDto): FeedPost {
        return FeedPost(
            id = dto.id,
            author = PostAuthor(
                id = dto.userId,
                username = dto.username ?: "Unknown",
                avatarUrl = dto.userAvatarUrl
            ),
            content = dto.content ?: "",
            mediaUrl = dto.mediaUrl,
            likeCount = dto.likeCount,
            commentCount = dto.commentCount,
            isLikedByCurrentUser = dto.likedByCurrentUser,
            createdAt = DateTimeUtils.parseDateTime(dto.createdAt),
            updatedAt = DateTimeUtils.parseDateTimeOrNull(dto.updatedAt)
        )
    }
    
    /**
     * Converts a list of PostDto to a list of Post domain models.
     *
     * @param dtos List of PostDto from the API
     * @return List of Post domain models
     */
    fun toDomainList(dtos: List<PostDto>): List<Post> {
        return dtos.map { toDomain(it) }
    }
    
    /**
     * Converts a list of FeedPostDto to a list of FeedPost domain models.
     *
     * @param dtos List of FeedPostDto from the API
     * @return List of FeedPost domain models
     */
    fun feedPostToDomainList(dtos: List<FeedPostDto>): List<FeedPost> {
        return dtos.map { feedPostToDomain(it) }
    }
}
