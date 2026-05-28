package com.example.myapplication1.model

data class Lunch(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val tags: String? = null,
    val userId: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
