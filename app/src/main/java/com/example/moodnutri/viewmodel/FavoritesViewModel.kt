package com.example.moodnutri.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.local.FavoriteRecipeEntity
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepository = LocalRecipeRepository(
        AppDatabase.getDatabase(application).favoriteRecipeDao()
    )
    private val firebaseRepository = FirebaseRecipeRepository()

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
        viewModelScope.launch {
            _isLoading.value = true

            // Cargar favoritos locales
            launch {
                localRepository.getAllFavorites().collect { locals ->
                    _localFavorites.value = locals
                }
            }

            // Cargar favoritos de Firebase
            launch {
                val result = firebaseRepository.getFavoriteRecipes()
                result.onSuccess { recipes ->
                    _firebaseFavorites.value = recipes
                }
            }

            _isLoading.value = false
        }
    }
}