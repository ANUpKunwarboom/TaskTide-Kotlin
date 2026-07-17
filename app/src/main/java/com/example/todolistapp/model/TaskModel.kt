package com.example.todolistapp.model

/**
 * TaskModel represents a single to-do item in the application.
 * It contains details like title, description, priority, and completion status.
 */
data class TaskModel(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val notes: String = "",
    val priority: String = "medium",
    val dueDate: Long? = null,
    val isDone: Boolean = false,
    val isStarred: Boolean = false,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val timerSeconds: Long = 0L,
    val sortOrder: Long = 0L
)

val CATEGORIES = listOf("Work", "Study", "Personal", "Health", "Finance", "Other")
val PRIORITIES = listOf("low", "medium", "high", "urgent")