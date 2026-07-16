package com.example.todolistapp.model

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "", // stores base64 data URL or empty
    val createdAt: Long = System.currentTimeMillis()
)
{

    fun toMap() : Map<String, Any?>{
        return mapOf(
            "name" to name,
            "email" to email,
            "avatarUrl" to avatarUrl,
            "createdAt" to createdAt
            )
    }
}