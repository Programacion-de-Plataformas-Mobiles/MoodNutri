package com.example.moodnutri.data.models.firebase

data class FirebaseRecipe(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val time: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUrl: String = "",
    val reason: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)