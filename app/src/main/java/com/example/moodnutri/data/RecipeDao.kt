package com.example.moodnutri.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Query("SELECT * FROM recipes")
    suspend fun getFavoriteRecipes(): List<Recipe>

    @Query("DELETE FROM recipes WHERE name = :name")
    suspend fun delete(name: String)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getFavoriteRecipesCount(): Int
}