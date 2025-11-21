// File: presentation/screens/favorites/FavoritesViewModel.kt
package com.example.moodnutri.presentation.screens.favorites

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.local.FavoriteRecipeEntity
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import com.example.moodnutri.domain.usecases.recipe.GetFavoriteRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepository = LocalRecipeRepository(
        AppDatabase.getDatabase(application).favoriteRecipeDao()
    )

    private val firebaseRepository = FirebaseRecipeRepository()

    private val getFavoriteRecipesUseCase = GetFavoriteRecipesUseCase(
        firebaseRepository = firebaseRepository,
        favoriteRecipeDao = AppDatabase.getDatabase(application).favoriteRecipeDao()
    )

    private val _localFavorites = MutableStateFlow<List<FavoriteRecipeEntity>>(emptyList())
    val localFavorites = _localFavorites.asStateFlow()

    private val _firebaseFavorites = MutableStateFlow<List<FirebaseRecipe>>(emptyList())
    val firebaseFavorites = _firebaseFavorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        Log.d("FavoritesVM", "=== LOADING FAVORITES ===")
        viewModelScope.launch {
            _isLoading.value = true

            // Cargar favoritos locales
            launch {
                localRepository.getAllFavorites().collect { locals ->
                    _localFavorites.value = locals
                    Log.d("FavoritesVM", "Local favorites loaded: ${locals.size} recipes")
                    locals.forEach { recipe ->
                        Log.d("FavoritesVM", "  - ${recipe.name}")
                    }
                }
            }

            // Cargar favoritos de Firebase usando Use Case
            launch {
                val result = getFavoriteRecipesUseCase()
                result.onSuccess { favoritesResult ->
                    _firebaseFavorites.value = favoritesResult.firebaseFavorites
                    Log.d("FavoritesVM", "Firebase favorites loaded: ${favoritesResult.firebaseFavorites.size} recipes")
                    favoritesResult.firebaseFavorites.forEach { recipe ->
                        Log.d("FavoritesVM", "  - ${recipe.name} (isFavorite: ${recipe.isFavorite})")
                    }
                }.onFailure { error ->
                    Log.e("FavoritesVM", "Error loading Firebase favorites", error)
                }
            }

            _isLoading.value = false
        }
    }
}