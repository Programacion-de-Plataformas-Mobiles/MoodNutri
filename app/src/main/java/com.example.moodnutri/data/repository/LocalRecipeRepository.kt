package com.example.moodnutri.data.repository

import com.example.moodnutri.data.local.FavoriteRecipeDao
import com.example.moodnutri.data.local.FavoriteRecipeEntity
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class LocalRecipeRepository(private val dao: FavoriteRecipeDao) {

    private val gson = Gson()

    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>> = dao.getAllFavorites()

    suspend fun addToFavorites(recipe: GeneratedRecipe, recipeId: String) {
        // Verificar si ya hay 5 favoritos
        val count = dao.getFavoriteCount()
        if (count >= 5) {
            // Eliminar el más antiguo para mantener 5 favoritos solamente
            dao.deleteOldestIfExceedsLimit()
        }

        val entity = FavoriteRecipeEntity(
            id = recipeId,
            name = recipe.name,
            time = recipe.time,
            ingredients = gson.toJson(recipe.ingredients),
            steps = gson.toJson(recipe.steps),
            imageUrl = recipe.image_url,
            reason = recipe.reason
        )

        dao.insertFavorite(entity)
    }

    suspend fun removeFromFavorites(recipeId: String) {
        val entity = dao.getFavoriteById(recipeId)
        entity?.let { dao.deleteFavorite(it) }
    }

    suspend fun isFavorite(recipeId: String): Boolean {
        return dao.getFavoriteById(recipeId) != null
    }

    fun convertEntityToGeneratedRecipe(entity: FavoriteRecipeEntity): GeneratedRecipe {
        return GeneratedRecipe(
            name = entity.name,
            time = entity.time,
            ingredients = gson.fromJson(entity.ingredients, Array<String>::class.java).toList(),
            steps = gson.fromJson(entity.steps, Array<String>::class.java).toList(),
            image_url = entity.imageUrl,
            reason = entity.reason
        )
    }
}