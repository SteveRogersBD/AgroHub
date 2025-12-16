package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.CommentDto
import com.example.agrohub.domain.model.Comment
import com.example.agrohub.domain.model.CommentAuthor

/**
 * Mapper for converting Comment DTOs to domain models.
 * Handles conversion from network layer representations to UI-friendly domain models.
 */
object CommentMapper {
    
    /**
     * Converts a CommentDto to a Comment domain model.
     *
     * @param dto The CommentDto from the API
     * @return Comment domain model
     * @throws Exception if required fields are missing or date parsing fails
     */
    fun toDomain(dto: CommentDto): Comment {
        return Comment(
            id = dto.id,
            postId = dto.postId,
            author = CommentAuthor(
                id = dto.userId,
                username = dto.username,
                avatarUrl = dto.userAvatarUrl
            ),
            content = dto.content,
            createdAt = DateTimeUtils.parseDateTime(dto.createdAt),
            updatedAt = DateTimeUtils.parseDateTimeOrNull(dto.updatedAt)
        )
    }
    
    /**
     * Converts a list of CommentDto to a list of Comment domain models.
     *
     * @param dtos List of CommentDto from the API
     * @return List of Comment domain models
     */
    fun toDomainList(dtos: List<CommentDto>): List<Comment> {
        return dtos.map { toDomain(it) }
    }
}
