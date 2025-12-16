package com.example.agrohub.data.mapper

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Utility functions for parsing date/time strings from API responses.
 * Handles ISO 8601 formatted timestamps.
 */
object DateTimeUtils {
    
    /**
     * Parses an ISO 8601 formatted timestamp string to LocalDateTime.
     * Supports various ISO 8601 formats including with and without timezone information.
     *
     * @param dateTimeString The ISO 8601 formatted date/time string
     * @return LocalDateTime representation of the timestamp
     * @throws DateTimeParseException if the string cannot be parsed
     */
    fun parseDateTime(dateTimeString: String): LocalDateTime {
        return try {
            // Try parsing as ZonedDateTime first (handles timezone info)
            ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
                .toLocalDateTime()
        } catch (e: DateTimeParseException) {
            try {
                // Fallback to LocalDateTime parsing (no timezone)
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e2: DateTimeParseException) {
                // Try with ISO_INSTANT format
                try {
                    val instant = java.time.Instant.parse(dateTimeString)
                    LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                } catch (e3: DateTimeParseException) {
                    throw DateTimeParseException(
                        "Unable to parse date/time string: $dateTimeString",
                        dateTimeString,
                        0
                    )
                }
            }
        }
    }
    
    /**
     * Safely parses an optional date/time string.
     * Returns null if the input is null or cannot be parsed.
     *
     * @param dateTimeString The optional ISO 8601 formatted date/time string
     * @return LocalDateTime representation or null
     */
    fun parseDateTimeOrNull(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            try {
                parseDateTime(it)
            } catch (e: DateTimeParseException) {
                null
            }
        }
    }
}
