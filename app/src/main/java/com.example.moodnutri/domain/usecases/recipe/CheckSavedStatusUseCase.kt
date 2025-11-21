package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.repository.FirebaseRecipeRepository

class CheckSavedStatusUseCase(private val firebaseRepository: FirebaseRecipeRepository) {

    suspend operator fun invoke(recipeId: String): Boolean {
        // Un receta se considera "guardada" si existe en Firebase.
        val result = firebaseRepository.getAllRecipes()
        return result.getOrNull()?.any { it.id == recipeId } ?: false
    }
}
