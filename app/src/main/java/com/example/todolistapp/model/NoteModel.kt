package com.example.todolistapp.model

data class NoteModel(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val color: Int = 0, // For note background color
    val createdAt: Long = System.currentTimeMillis()
)
