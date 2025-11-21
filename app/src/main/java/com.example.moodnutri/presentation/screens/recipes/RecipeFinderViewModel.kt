package com.example.moodnutri.presentation.screens.recipes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.remote.RetrofitInstance
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import com.example.moodnutri.data.repository.RecipeRepository
import com.example.moodnutri.domain.usecases.recipe.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeFinderViewModel(application: Application) : AndroidViewModel(application) {

    // Use Cases
    private val searchRecipeUseCase = SearchRecipeUseCase(
        recipeRepository = RecipeRepository(),
        openAiService = RetrofitInstance.openAiApi,
        preferencesManager = UserPreferencesManager(application)
    )

    private val saveRecipeUseCase = SaveRecipeUseCase(
        firebaseRepository = FirebaseRecipeRepository()
    )

    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(
        firebaseRepository = FirebaseRecipeRepository(),
        localRepository = LocalRecipeRepository(
            AppDatabase.getDatabase(application).favoriteRecipeDao()
        )
    )

    private val checkFavoriteStatusUseCase = CheckFavoriteStatusUseCase(
        firebaseRepository = FirebaseRecipeRepository()
    )

    private val checkSavedStatusUseCase = CheckSavedStatusUseCase(
        firebaseRepository = FirebaseRecipeRepository()
    )

    // UI State
    private val _uiState = MutableStateFlow<RecipeFinderState>(RecipeFinderState.Idle)
    val uiState: StateFlow<RecipeFinderState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _saveInProgress = MutableStateFlow(false)
    val saveInProgress: StateFlow<Boolean> = _saveInProgress.asStateFlow()

    private val _favoriteInProgress = MutableStateFlow(false)
    val favoriteInProgress: StateFlow<Boolean> = _favoriteInProgress.asStateFlow()

    // Cache
    private var lastSearchedIngredients: List<String>? = null
    private var lastSearchedMood: String? = null
    private var lastSearchedTime: String? = null

    fun searchRecipe(userIngredients: List<String>, mood: String, availableTime: String) {
        if (_uiState.value is RecipeFinderState.Success &&
            lastSearchedIngredients == userIngredients &&
            lastSearchedMood == mood &&
            lastSearchedTime == availableTime) {
            return
        }

        _uiState.value = RecipeFinderState.Loading
        _isFavorite.value = false
        _isSaved.value = false

        lastSearchedIngredients = userIngredients
        lastSearchedMood = mood
        lastSearchedTime = availableTime

        viewModelScope.launch(Dispatchers.IO) {
            val params = SearchRecipeParams(userIngredients, mood, availableTime)
            val result = searchRecipeUseCase(params)

            result.onSuccess { recipeResult ->
                _uiState.value = RecipeFinderState.Success(
                    recipe = recipeResult.recipe,
                    recipeId = recipeResult.recipeId
                )
                checkStatuses(recipeResult.recipeId)
            }.onFailure { error ->
                Log.e("RecipeFinderVM", "Error searching recipe", error)
                _uiState.value = RecipeFinderState.Error(
                    error.message ?: "Error searching for recipes"
                )
            }
        }
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (state !is RecipeFinderState.Success || _isSaved.value) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _saveInProgress.value = true

            val params = SaveRecipeParams(
                recipeId = state.recipeId,
                recipe = state.recipe,
                isFavorite = _isFavorite.value
            )

            val result = saveRecipeUseCase(params)

            result.onSuccess {
                _isSaved.value = true
                Log.d("RecipeFinderVM", "Recipe saved successfully")
            }.onFailure { error ->
                Log.e("RecipeFinderVM", "Error saving recipe", error)
            }

            _saveInProgress.value = false
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        if (state !is RecipeFinderState.Success) return

        val newFavoriteStatus = !_isFavorite.value

        viewModelScope.launch(Dispatchers.IO) {
            _favoriteInProgress.value = true

            val params = ToggleFavoriteParams(
                recipeId = state.recipeId,
                recipe = state.recipe,
                newFavoriteStatus = newFavoriteStatus
            )

            val result = toggleFavoriteUseCase(params)

            result.onSuccess {
                _isFavorite.value = newFavoriteStatus
                if (newFavoriteStatus) {
                    _isSaved.value = true
                }
                Log.d("RecipeFinderVM", "Toggle favorite successful")
            }.onFailure { error ->
                Log.e("RecipeFinderVM", "Error toggling favorite", error)
            }

            _favoriteInProgress.value = false
        }
    }

    private suspend fun checkStatuses(recipeId: String) {
        _isFavorite.value = checkFavoriteStatusUseCase(recipeId)
        _isSaved.value = checkSavedStatusUseCase(recipeId)
    }
}
