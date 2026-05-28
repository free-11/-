package com.example.myapplication1.model

data class LunchHistory(
    val id: Long? = null,
    val userId: Long,
    val lunchName: String,
    val lunchDescription: String? = null,
    val tags: String? = null,
    val createdAt: String? = null
)
