package com.example.agrohub.domain.model

data class FieldTask(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
