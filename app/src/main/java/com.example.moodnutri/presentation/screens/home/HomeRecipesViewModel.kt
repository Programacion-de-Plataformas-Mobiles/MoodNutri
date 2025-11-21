// File: presentation/screens/home/HomeRecipesViewModel.kt
package com.example.moodnutri.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.domain.usecases.recipe.GetSavedRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeRecipesViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRecipeRepository()
    private val getSavedRecipesUseCase = GetSavedRecipesUseCase(firebaseRepository)

    private val _savedRecipes = MutableStateFlow<List<FirebaseRecipe>>(emptyList())
    val savedRecipes = _savedRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun loadSavedRecipes() {
        Log.d("HomeRecipesVM", "==============================================")
        Log.d("HomeRecipesVM", "=== LOADING SAVED RECIPES FOR HOME SCREEN ===")
        Log.d("HomeRecipesVM", "==============================================")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("HomeRecipesVM", "Calling getSavedRecipesUseCase()...")
                val result = getSavedRecipesUseCase()

                result.onSuccess { recipes ->
                    Log.d("HomeRecipesVM", "SUCCESS! Received ${recipes.size} recipes")

                    if (recipes.isEmpty()) {
                        Log.w("HomeRecipesVM", "WARNING: Recipe list is EMPTY!")
                        _errorMessage.value = "No saved recipes found"
                    } else {
                        Log.d("HomeRecipesVM", "Recipe list:")
                        recipes.forEachIndexed { index, recipe ->
                            Log.d("HomeRecipesVM", "  ${index + 1}. ${recipe.name}")
                            Log.d("HomeRecipesVM", "     - ID: ${recipe.id}")
                            Log.d("HomeRecipesVM", "     - isFavorite: ${recipe.isFavorite}")
                        }
                    }

                    _savedRecipes.value = recipes
                    Log.d("HomeRecipesVM", "State updated: _savedRecipes.value has ${_savedRecipes.value.size} recipes")

                }.onFailure { error ->
                    Log.e("HomeRecipesVM", "FAILURE! Error loading recipes")
                    Log.e("HomeRecipesVM", "Error message: ${error.message}")
                    error.printStackTrace()
                    _errorMessage.value = error.message ?: "Unknown error occurred"
                    _savedRecipes.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("HomeRecipesVM", "EXCEPTION in loadSavedRecipes()")
                Log.e("HomeRecipesVM", "Exception message: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _savedRecipes.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d("HomeRecipesVM", "Loading finished. isLoading = false")
                Log.d("HomeRecipesVM", "Final state:")
                Log.d("HomeRecipesVM", "   - savedRecipes count: ${_savedRecipes.value.size}")
                Log.d("HomeRecipesVM", "   - isLoading: ${_isLoading.value}")
                Log.d("HomeRecipesVM", "   - errorMessage: ${_errorMessage.value}")
                Log.d("HomeRecipesVM", "==============================================")
            }
        }
    }
}