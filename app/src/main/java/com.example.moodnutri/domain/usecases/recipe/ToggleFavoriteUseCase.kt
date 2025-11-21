package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import com.example.moodnutri.domain.usecases.UseCase

// ============================================
// ToggleFavoriteUseCase.kt
// ============================================
data class ToggleFavoriteParams(
    val recipeId: String,
    val recipe: GeneratedRecipe,
    val newFavoriteStatus: Boolean
)

class ToggleFavoriteUseCase(
    private val firebaseRepository: FirebaseRecipeRepository,
    private val localRepository: LocalRecipeRepository
) : UseCase<ToggleFavoriteParams, Result<Unit>> {

    override suspend fun invoke(params: ToggleFavoriteParams): Result<Unit> {
        return try {
            val firebaseRecipe = FirebaseRecipe(
                id = params.recipeId,
                name = params.recipe.name,
                time = params.recipe.time,
                ingredients = params.recipe.ingredients,
                steps = params.recipe.steps,
                imageUrl = params.recipe.image_url,
                reason = params.recipe.reason,
                isFavorite = params.newFavoriteStatus
            )

            val result = firebaseRepository.toggleFavorite(firebaseRecipe, params.newFavoriteStatus)

            if (result.isSuccess) {
                if (params.newFavoriteStatus) {
                    localRepository.addToFavorites(params.recipe, params.recipeId)
                } else {
                    localRepository.removeFromFavorites(params.recipeId)
                }
            }

            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}