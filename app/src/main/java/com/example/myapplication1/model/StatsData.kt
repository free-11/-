package com.example.myapplication1.model

data class StatsData(
    val totalSpins: Int,
    val totalDishes: Int,
    val topDishes: List<TopDish>,
    val byDayOfWeek: Map<String, Long>
)

data class TopDish(
    val name: String,
    val count: Long
)
