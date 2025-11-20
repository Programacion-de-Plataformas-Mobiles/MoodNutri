// data/local/FavoriteRecipeEntity.kt
package com.example.moodnutri.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey val name: String,
    val jsonDetails: String, // Guardamos todo el GeneratedRecipe como JSON
    val isFavorite: Boolean = true
)