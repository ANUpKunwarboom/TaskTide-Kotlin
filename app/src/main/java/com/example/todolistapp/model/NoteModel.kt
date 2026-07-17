package com.example.todolistapp.model

/**
 * NoteModel represents a single note entry.
 * It stores the note's content, title, and visual properties like color.
 */
data class NoteModel(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val color: Int = 0, // For note background color
    val createdAt: Long = System.currentTimeMillis()
)
