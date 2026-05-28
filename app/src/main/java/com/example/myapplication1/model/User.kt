package com.example.myapplication1.model

data class User(
    val id: Long? = null,
    val email: String,
    val password: String? = null,
    val nickname: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
