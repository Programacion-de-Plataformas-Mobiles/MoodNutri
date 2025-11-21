package com.example.moodnutri.presentation.screens.recipes

import com.example.moodnutri.data.models.openai.GeneratedRecipe

// ============================================
// RecipeFinderState.kt (presentation/screens/recipes/)
// ============================================
sealed interface RecipeFinderState {
    object Idle : RecipeFinderState
    object Loading : RecipeFinderState
    data class Success(val recipe: GeneratedRecipe, val recipeId: String) : RecipeFinderState
    data class Error(val message: String) : RecipeFinderState
}