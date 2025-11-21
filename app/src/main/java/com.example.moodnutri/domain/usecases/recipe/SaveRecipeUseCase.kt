package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.domain.usecases.UseCase

// ============================================
// SaveRecipeUseCase.kt
// ============================================
data class SaveRecipeParams(
    val recipeId: String,
    val recipe: GeneratedRecipe,
    val isFavorite: Boolean
)

class SaveRecipeUseCase(
    private val firebaseRepository: FirebaseRecipeRepository
) : UseCase<SaveRecipeParams, Result<Unit>> {

    override suspend fun invoke(params: SaveRecipeParams): Result<Unit> {
        return try {
            val firebaseRecipe = FirebaseRecipe(
                id = params.recipeId,
                name = params.recipe.name,
                time = params.recipe.time,
                ingredients = params.recipe.ingredients,
                steps = params.recipe.steps,
                imageUrl = params.recipe.image_url,
                reason = params.recipe.reason,
                isFavorite = params.isFavorite
            )

            firebaseRepository.saveRecipe(firebaseRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}