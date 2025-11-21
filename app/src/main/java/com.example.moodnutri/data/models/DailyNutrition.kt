package com.example.moodnutri.data.models

data class DailyNutrition(
    val date: String = "", // formato: yyyy-MM-dd
    val userId: String = "",
    val caloriesConsumed: Int = 0,
    val proteinConsumed: Int = 0,
    val carbsConsumed: Int = 0,
    val meals: List<String> = emptyList() // IDs de recetas consumidas
)