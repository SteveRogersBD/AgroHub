package com.example.agrohub.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Pagination DTOs for backend API integration
 */

@JsonClass(generateAdapter = true)
data class PagedResponseDto<T>(
    @Json(name = "content")
    val content: List<T>,
    @Json(name = "pageable")
    val pageable: PageableDto,
    @Json(name = "totalElements")
    val totalElements: Int,
    @Json(name = "totalPages")
    val totalPages: Int,
    @Json(name = "last")
    val last: Boolean
)

@JsonClass(generateAdapter = true)
data class PageableDto(
    @Json(name = "pageNumber")
    val pageNumber: Int,
    @Json(name = "pageSize")
    val pageSize: Int
)
