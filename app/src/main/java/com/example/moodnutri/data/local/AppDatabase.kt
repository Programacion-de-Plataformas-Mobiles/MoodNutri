// data/local/AppDatabase.kt
package com.example.moodnutri.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moodnutri.data.Recipe
import com.example.moodnutri.data.RecipeDao

@Database(entities = [FavoriteRecipeEntity::class, Recipe::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
    abstract fun recipeDao(): RecipeDao
}