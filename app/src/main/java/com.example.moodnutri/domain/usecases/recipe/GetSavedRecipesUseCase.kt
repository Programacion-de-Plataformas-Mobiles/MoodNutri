package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// GetSavedRecipesUseCase.kt
// ============================================
class GetSavedRecipesUseCase(
    private val firebaseRepository: FirebaseRecipeRepository
) : UseCaseNoParams<Result<List<FirebaseRecipe>>> {

    override suspend fun invoke(): Result<List<FirebaseRecipe>> {
        return firebaseRepository.getSavedRecipes()
    }
}