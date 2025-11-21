package com.example.moodnutri.presentation.screens.recipes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import com.example.moodnutri.domain.usecases.recipe.CheckFavoriteStatusUseCase
import com.example.moodnutri.domain.usecases.recipe.GetRecipeDetailUseCase
import com.example.moodnutri.domain.usecases.recipe.ToggleFavoriteParams
import com.example.moodnutri.domain.usecases.recipe.ToggleFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseRecipeRepository()
    private val localRepository = LocalRecipeRepository(
        AppDatabase.getDatabase(application).favoriteRecipeDao()
    )

    // Use Cases
    private val getRecipeDetailUseCase = GetRecipeDetailUseCase(firebaseRepository)
    private val checkFavoriteStatusUseCase = CheckFavoriteStatusUseCase(firebaseRepository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(
        firebaseRepository = firebaseRepository,
        localRepository = localRepository
    )

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
                val result = getRecipeDetailUseCase(recipeId)

                result.onSuccess { foundRecipe ->
                    _recipe.value = foundRecipe

                    if (foundRecipe != null) {
                        _isFavorite.value = checkFavoriteStatusUseCase(recipeId)
                        Log.d("RecipeDetailVM", "Recipe loaded: ${foundRecipe.name}")
                        Log.d("RecipeDetailVM", "Is favorite: ${_isFavorite.value}")
                    }
                }.onFailure { error ->
                    Log.e("RecipeDetailVM", "Error loading recipe", error)
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

        Log.d("RecipeDetailVM", "=== TOGGLE FAVORITE ===")
        Log.d("RecipeDetailVM", "Recipe ID: ${currentRecipe.id}")
        Log.d("RecipeDetailVM", "New favorite status: $newFavoriteStatus")

        viewModelScope.launch(Dispatchers.IO) {
            _favoriteInProgress.value = true
            try {
                val generatedRecipe = GeneratedRecipe(
                    name = currentRecipe.name,
                    time = currentRecipe.time,
                    ingredients = currentRecipe.ingredients,
                    steps = currentRecipe.steps,
                    image_url = currentRecipe.imageUrl,
                    reason = currentRecipe.reason
                )

                val params = ToggleFavoriteParams(
                    recipeId = currentRecipe.id,
                    recipe = generatedRecipe,
                    newFavoriteStatus = newFavoriteStatus
                )

                val result = toggleFavoriteUseCase(params)

                result.onSuccess {
                    _isFavorite.value = newFavoriteStatus
                    _recipe.value = currentRecipe.copy(isFavorite = newFavoriteStatus)
                    Log.d("RecipeDetailVM", "Toggle favorite successful")
                }.onFailure { error ->
                    Log.e("RecipeDetailVM", "Error toggling favorite", error)
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailVM", "Error toggling favorite", e)
            } finally {
                _favoriteInProgress.value = false
            }
        }
    }
}
