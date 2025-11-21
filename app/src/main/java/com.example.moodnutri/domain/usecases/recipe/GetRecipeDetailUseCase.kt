package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.domain.usecases.UseCase

// ============================================
// GetRecipeDetailUseCase.kt
// ============================================
class GetRecipeDetailUseCase(
    private val firebaseRepository: FirebaseRecipeRepository
) : UseCase<String, Result<FirebaseRecipe?>> {

    override suspend fun invoke(params: String): Result<FirebaseRecipe?> {
        return try {
            val result = firebaseRepository.getAllRecipes()
            result.map { recipes ->
                recipes.find { it.id == params }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}