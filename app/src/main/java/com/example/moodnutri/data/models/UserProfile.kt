package com.example.moodnutri.data.models

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val dailyCalorieGoal: Int = 2000,
    val dailyProteinGoal: Int = 150, // en gramos
    val dailyCarbsGoal: Int = 250,   // en gramos
    val language: String = "en", // en, es, fr
    val themeMode: String = "system" // light, dark, o del system
)