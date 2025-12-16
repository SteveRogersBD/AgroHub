package com.example.agrohub.data.mapper

import com.example.agrohub.data.remote.dto.UserProfileDto
import com.example.agrohub.domain.model.User

/**
 * Mapper for converting User DTOs to domain models.
 * Handles conversion from network layer representations to UI-friendly domain models.
 */
object UserMapper {
    
    /**
     * Converts a UserProfileDto to a User domain model.
     * Provides default values for optional fields to ensure non-null domain model fields.
     *
     * @param dto The UserProfileDto from the API
     * @return User domain model
     * @throws Exception if required fields are missing or date parsing fails
     */
    fun toDomain(dto: UserProfileDto): User {
        return User(
            id = dto.id,
            email = dto.email,
            username = dto.username,
            name = dto.name ?: "",
            bio = dto.bio ?: "",
            avatarUrl = dto.avatarUrl,
            location = dto.location ?: "",
            website = dto.website,
            createdAt = DateTimeUtils.parseDateTime(dto.createdAt),
            updatedAt = DateTimeUtils.parseDateTimeOrNull(dto.updatedAt)
        )
    }
    
    /**
     * Converts a list of UserProfileDto to a list of User domain models.
     *
     * @param dtos List of UserProfileDto from the API
     * @return List of User domain models
     */
    fun toDomainList(dtos: List<UserProfileDto>): List<User> {
        return dtos.map { toDomain(it) }
    }
}
