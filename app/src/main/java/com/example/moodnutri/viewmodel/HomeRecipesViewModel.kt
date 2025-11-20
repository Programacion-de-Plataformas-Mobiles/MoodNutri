package com.example.moodnutri.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeRecipesViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRecipeRepository()

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
                Log.d("HomeRecipesVM", " Calling firebaseRepository.getSavedRecipes()...")
                val result = firebaseRepository.getSavedRecipes()

                result.onSuccess { recipes ->
                    Log.d("HomeRecipesVM", " SUCCESS! Received ${recipes.size} recipes from Firebase")

                    if (recipes.isEmpty()) {
                        Log.w("HomeRecipesVM", " WARNING: Recipe list is EMPTY!")
                        Log.w("HomeRecipesVM", "Possible reasons:")
                        Log.w("HomeRecipesVM", "1. No recipes have been saved yet")
                        Log.w("HomeRecipesVM", "2. All recipes are marked as favorites (isFavorite=true)")
                        Log.w("HomeRecipesVM", "3. Firebase query is not finding the documents")
                        _errorMessage.value = "No saved recipes found"
                    } else {
                        Log.d("HomeRecipesVM", " Recipe list:")
                        recipes.forEachIndexed { index, recipe ->
                            Log.d("HomeRecipesVM", "  ${index + 1}. ${recipe.name}")
                            Log.d("HomeRecipesVM", "     - ID: ${recipe.id}")
                            Log.d("HomeRecipesVM", "     - isFavorite: ${recipe.isFavorite}")
                            Log.d("HomeRecipesVM", "     - time: ${recipe.time}")
                            Log.d("HomeRecipesVM", "     - imageUrl: ${recipe.imageUrl}")
                        }
                    }

                    _savedRecipes.value = recipes
                    Log.d("HomeRecipesVM", " State updated: _savedRecipes.value has ${_savedRecipes.value.size} recipes")

                }.onFailure { error ->
                    Log.e("HomeRecipesVM", " FAILURE! Error loading recipes")
                    Log.e("HomeRecipesVM", "Error message: ${error.message}")
                    Log.e("HomeRecipesVM", "Error type: ${error.javaClass.simpleName}")
                    error.printStackTrace()
                    _errorMessage.value = error.message ?: "Unknown error occurred"
                    _savedRecipes.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("HomeRecipesVM", " EXCEPTION in loadSavedRecipes()")
                Log.e("HomeRecipesVM", "Exception message: ${e.message}")
                Log.e("HomeRecipesVM", "Exception type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _savedRecipes.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d("HomeRecipesVM", " Loading finished. isLoading = false")
                Log.d("HomeRecipesVM", " Final state:")
                Log.d("HomeRecipesVM", "   - savedRecipes count: ${_savedRecipes.value.size}")
                Log.d("HomeRecipesVM", "   - isLoading: ${_isLoading.value}")
                Log.d("HomeRecipesVM", "   - errorMessage: ${_errorMessage.value}")
                Log.d("HomeRecipesVM", "==============================================")
            }
        }
    }
}