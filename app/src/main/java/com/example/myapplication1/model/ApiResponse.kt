package com.example.myapplication1.model

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)
