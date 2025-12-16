package com.example.agrohub.domain.model

/**
 * Generic domain model representing paginated data.
 * This provides a consistent structure for handling paginated responses throughout the application.
 *
 * @param T The type of items in the page
 * @property items The list of items in the current page
 * @property currentPage The current page number (0-indexed)
 * @property pageSize The number of items per page
 * @property totalElements The total number of elements across all pages
 * @property totalPages The total number of pages
 * @property isLastPage Whether this is the last page of results
 */
data class PagedData<T>(
    val items: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    val totalElements: Int,
    val totalPages: Int,
    val isLastPage: Boolean
)
