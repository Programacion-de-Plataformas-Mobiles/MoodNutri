package com.example.moodnutri.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseRecipeRepository()
    private val localRepository = LocalRecipeRepository(
        AppDatabase.getDatabase(application).favoriteRecipeDao()
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
                val result = firebaseRepository.getAllRecipes()
                result.onSuccess { recipes ->
                    val foundRecipe = recipes.find { it.id == recipeId }
                    _recipe.value = foundRecipe

                    // Verificar estado de favorito en ambos lugares
                    val isFirebaseFavorite = foundRecipe?.isFavorite ?: false
                    val isLocalFavorite = localRepository.isFavorite(recipeId)
                    _isFavorite.value = isFirebaseFavorite || isLocalFavorite

                    Log.d("RecipeDetailVM", "Recipe loaded: ${foundRecipe?.name}")
                    Log.d("RecipeDetailVM", "Firebase favorite: $isFirebaseFavorite, Local favorite: $isLocalFavorite")
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
                // Actualizar en Firebase
                val firebaseResult = firebaseRepository.toggleFavorite(currentRecipe, newFavoriteStatus)

                if (firebaseResult.isSuccess) {
                    // Si se marcó como favorito, también guardar en Room local
                    if (newFavoriteStatus) {
                        Log.d("RecipeDetailVM", "Agregando a favoritos locales (Room)...")
                        val generatedRecipe = GeneratedRecipe(
                            name = currentRecipe.name,
                            time = currentRecipe.time,
                            ingredients = currentRecipe.ingredients,
                            steps = currentRecipe.steps,
                            image_url = currentRecipe.imageUrl,
                            reason = currentRecipe.reason
                        )
                        localRepository.addToFavorites(generatedRecipe, currentRecipe.id)
                        Log.d("RecipeDetailVM", " Receta agregada a favoritos locales")
                    } else {
                        // Si se quitó de favoritos, remover de Room
                        Log.d("RecipeDetailVM", "Removiendo de favoritos locales (Room)...")
                        localRepository.removeFromFavorites(currentRecipe.id)
                        Log.d("RecipeDetailVM", " Receta removida de favoritos locales")
                    }

                    _isFavorite.value = newFavoriteStatus
                    _recipe.value = currentRecipe.copy(isFavorite = newFavoriteStatus)
                    Log.d("RecipeDetailVM", " Toggle favorite successful")
                } else {
                    Log.e("RecipeDetailVM", " Error toggling favorite in Firebase")
                }
            } catch (e: Exception) {
                Log.e("RecipeDetailVM", " Error toggling favorite", e)
            } finally {
                _favoriteInProgress.value = false
            }
        }
    }
}