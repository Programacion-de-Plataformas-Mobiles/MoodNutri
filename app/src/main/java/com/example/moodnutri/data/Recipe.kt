package com.example.moodnutri.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val name: String,
    val details: String,
    var isFavorite: Boolean = false
)