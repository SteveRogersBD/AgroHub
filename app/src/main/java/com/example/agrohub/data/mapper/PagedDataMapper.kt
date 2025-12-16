package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.PagedResponseDto
import com.example.agrohub.domain.model.PagedData

/**
 * Generic mapper for converting paginated DTOs to domain models.
 * Provides a reusable way to map any paginated API response to the domain PagedData model.
 */
object PagedDataMapper {
    
    /**
     * Converts a PagedResponseDto to a PagedData domain model.
     * Uses a provided mapper function to convert individual items from DTO to domain type.
     *
     * @param T The DTO type
     * @param R The domain model type
     * @param dto The PagedResponseDto from the API
     * @param itemMapper Function to convert individual items from DTO to domain model
     * @return PagedData domain model containing the mapped items
     */
    fun <T, R> toDomain(
        dto: PagedResponseDto<T>,
        itemMapper: (T) -> R
    ): PagedData<R> {
        return PagedData(
            items = dto.content.map(itemMapper),
            currentPage = dto.pageable.pageNumber,
            pageSize = dto.pageable.pageSize,
            totalElements = dto.totalElements,
            totalPages = dto.totalPages,
            isLastPage = dto.last
        )
    }
    
    /**
     * Convenience method for mapping paginated user responses.
     *
     * @param dto The PagedResponseDto containing UserProfileDto items
     * @return PagedData containing User domain models
     */
    fun toUserPagedData(dto: PagedResponseDto<com.example.agrohub.data.remote.dto.UserProfileDto>): PagedData<com.example.agrohub.domain.model.User> {
        return toDomain(dto, UserMapper::toDomain)
    }
    
    /**
     * Convenience method for mapping paginated post responses.
     *
     * @param dto The PagedResponseDto containing PostDto items
     * @return PagedData containing Post domain models
     */
    fun toPostPagedData(dto: PagedResponseDto<com.example.agrohub.data.remote.dto.PostDto>): PagedData<com.example.agrohub.domain.model.Post> {
        return toDomain(dto, PostMapper::toDomain)
    }
    
    /**
     * Convenience method for mapping paginated feed post responses.
     *
     * @param dto The PagedResponseDto containing FeedPostDto items
     * @return PagedData containing FeedPost domain models
     */
    fun toFeedPostPagedData(dto: PagedResponseDto<com.example.agrohub.data.remote.dto.FeedPostDto>): PagedData<com.example.agrohub.domain.model.FeedPost> {
        return toDomain(dto, PostMapper::feedPostToDomain)
    }
    
    /**
     * Convenience method for mapping paginated comment responses.
     *
     * @param dto The PagedResponseDto containing CommentDto items
     * @return PagedData containing Comment domain models
     */
    fun toCommentPagedData(dto: PagedResponseDto<com.example.agrohub.data.remote.dto.CommentDto>): PagedData<com.example.agrohub.domain.model.Comment> {
        return toDomain(dto, CommentMapper::toDomain)
    }
    
    /**
     * Convenience method for mapping paginated notification responses.
     *
     * @param dto The PagedResponseDto containing NotificationDto items
     * @return PagedData containing Notification domain models
     */
    fun toNotificationPagedData(dto: PagedResponseDto<com.example.agrohub.data.remote.dto.NotificationDto>): PagedData<com.example.agrohub.domain.model.Notification> {
        return toDomain(dto, NotificationMapper::toDomain)
    }
}
