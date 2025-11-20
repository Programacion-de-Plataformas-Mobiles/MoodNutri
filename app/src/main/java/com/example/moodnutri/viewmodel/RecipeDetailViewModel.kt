package com.example.moodnutri.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRecipeRepository()

    private val _recipe = MutableStateFlow<FirebaseRecipe?>(null)
    val recipe = _recipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _favoriteInProgress = MutableStateFlow(false)
    val favoriteInProgress = _favoriteInProgress.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = firebaseRepository.getAllRecipes()
                result.onSuccess { recipes ->
                    val foundRecipe = recipes.find { it.id == recipeId }
                    _recipe.value = foundRecipe
                    _isFavorite.value = foundRecipe?.isFavorite ?: false
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailVM", "Error loading recipe", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentRecipe = _recipe.value ?: return
        val newFavoriteStatus = !_isFavorite.value

        viewModelScope.launch(Dispatchers.IO) {
            _favoriteInProgress.value = true
            try {
                val result = firebaseRepository.toggleFavorite(currentRecipe, newFavoriteStatus)

                if (result.isSuccess) {
                    _isFavorite.value = newFavoriteStatus
                    _recipe.value = currentRecipe.copy(isFavorite = newFavoriteStatus)
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailVM", "Error toggling favorite", e)
            } finally {
                _favoriteInProgress.value = false
            }
        }
    }
}