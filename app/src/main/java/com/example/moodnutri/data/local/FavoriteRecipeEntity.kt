package com.example.moodnutri.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val time: String,
    val ingredients: String, // JSON string
    val steps: String, // JSON string
    val imageUrl: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)