package com.example.moodnutri.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {

    @Query("SELECT * FROM favorite_recipes ORDER BY timestamp DESC LIMIT 5")
    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT * FROM favorite_recipes WHERE id = :recipeId")
    suspend fun getFavoriteById(recipeId: String): FavoriteRecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(recipe: FavoriteRecipeEntity)

    @Delete
    suspend fun deleteFavorite(recipe: FavoriteRecipeEntity)

    @Query("SELECT COUNT(*) FROM favorite_recipes")
    suspend fun getFavoriteCount(): Int

    @Query("DELETE FROM favorite_recipes WHERE id NOT IN (SELECT id FROM favorite_recipes ORDER BY timestamp DESC LIMIT 5)")
    suspend fun deleteOldestIfExceedsLimit()
}