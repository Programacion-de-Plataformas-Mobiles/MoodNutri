// data/OfflineFavoritesRepository.kt
package com.example.moodnutri.data

import com.example.moodnutri.data.local.FavoriteRecipeDao
import com.example.moodnutri.data.local.FavoriteRecipeEntity
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OfflineFavoritesRepository @Inject constructor(
    private val dao: FavoriteRecipeDao,
    private val gson: Gson
) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // Solo favoritos (máx 5)
    fun getLocalFavorites(): Flow<List<GeneratedRecipe>> = dao.getAllFavorites().map { list ->
        list.map { gson.fromJson(it.jsonDetails, GeneratedRecipe::class.java) }
    }

    suspend fun addToFavorites(recipe: GeneratedRecipe) {
        val count = dao.getCount()
        if (count >= 5) return // límite offline

        val entity = FavoriteRecipeEntity(
            name = recipe.name,
            jsonDetails = gson.toJson(recipe)
        )
        dao.insert(entity)

        // También guardamos en Firebase (para sync)
        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("favorites").child(recipe.name).setValue(recipe).await()
    }

    suspend fun removeFromFavorites(recipeName: String) {
        dao.deleteByName(recipeName)

        val userId = auth.currentUser?.uid ?: return
        db.child("users").child(userId).child("favorites").child(recipeName).removeValue().await()
    }

    suspend fun isFavorite(recipeName: String): Boolean {
        // 1. Get the current list from the Flow
        val currentList = dao.getAllFavorites().firstOrNull() ?: emptyList()

        // 2. Check if any item inside that list matches the name
        return currentList.any { it.name == recipeName }
    }

}