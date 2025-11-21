package com.example.moodnutri.domain.usecases.recipe

import com.example.moodnutri.data.repository.FirebaseRecipeRepository

class CheckFavoriteStatusUseCase(private val firebaseRepository: FirebaseRecipeRepository) {

    suspend operator fun invoke(recipeId: String): Boolean {
        // Obtenemos todas las recetas y buscamos la que coincida con el ID
        val result = firebaseRepository.getAllRecipes()
        return result.getOrNull()?.find { it.id == recipeId }?.isFavorite ?: false
    }
}
