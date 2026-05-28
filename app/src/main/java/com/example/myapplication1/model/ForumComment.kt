package com.example.myapplication1.model

data class ForumComment(
    val id: Long? = null,
    val postId: Long? = null,
    val userId: Long? = null,
    val nickname: String? = null,
    val content: String? = null,
    val createdAt: String? = null
)
