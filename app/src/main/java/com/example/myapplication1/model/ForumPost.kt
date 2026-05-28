package com.example.myapplication1.model

data class ForumPost(
    val id: Long? = null,
    val userId: Long? = null,
    val nickname: String? = null,
    val content: String? = null,
    val dishName: String? = null,
    val likes: Int? = 0,
    val likedUserIds: String? = null,
    val createdAt: String? = null
)
