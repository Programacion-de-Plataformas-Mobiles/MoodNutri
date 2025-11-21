package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.local.FavoriteRecipeDao
import com.example.moodnutri.data.local.FavoriteRecipeEntity
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// GetFavoriteRecipesUseCase.kt
// ============================================
data class FavoriteRecipesResult(
    val localFavorites: List<FavoriteRecipeEntity>,
    val firebaseFavorites: List<FirebaseRecipe>
)

class GetFavoriteRecipesUseCase(
    private val firebaseRepository: FirebaseRecipeRepository,
    private val favoriteRecipeDao: FavoriteRecipeDao
) : UseCaseNoParams<Result<FavoriteRecipesResult>> {

    override suspend fun invoke(): Result<FavoriteRecipesResult> {
        return try {
            val firebaseResult = firebaseRepository.getFavoriteRecipes()
            val firebaseFavorites = firebaseResult.getOrNull() ?: emptyList()

            // Local favorites are collected via Flow in ViewModel
            Result.success(FavoriteRecipesResult(emptyList(), firebaseFavorites))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}