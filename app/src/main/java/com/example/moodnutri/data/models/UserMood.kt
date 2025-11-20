package com.example.moodnutri.data.models

data class UserMood(
    val userId: String = "",
    val mood: String = "",
    val emoji: String = "😊",
    val timestamp: Long = System.currentTimeMillis()
)