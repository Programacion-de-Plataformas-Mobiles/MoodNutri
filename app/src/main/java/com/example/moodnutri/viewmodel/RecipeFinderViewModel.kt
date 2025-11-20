package com.example.moodnutri.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.NutritionRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

sealed interface RecipeFinderState {
    object Idle : RecipeFinderState
    object Loading : RecipeFinderState
    data class Success(val recipe: GeneratedRecipe, val recipeId: String) : RecipeFinderState
    data class Error(val message: String) : RecipeFinderState
}

class RecipeFinderViewModel(application: Application) : AndroidViewModel(application) {

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

    private val firebaseRepository = FirebaseRecipeRepository()
    private val preferencesManager = UserPreferencesManager(application)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun searchRecipe(userIngredients: List<String>, mood: String, availableTime: String) {
        _uiState.value = RecipeFinderState.Loading
        _isFavorite.value = false
        _isSaved.value = false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Obtener el idioma actual
                val language = preferencesManager.language.firstOrNull() ?: "en"
                val languageName = when(language) {
                    "es" -> "Spanish"
                    "fr" -> "French"
                    else -> "English"
                }

                val ingredientList = userIngredients.joinToString(", ")

                val prompt = """
                    You are a creative chef assistant. Based on the following information, create a unique recipe:
                    - Ingredients available: $ingredientList
                    - User's mood: $mood
                    - Available cooking time: $availableTime
                    
                    IMPORTANT: Generate the entire recipe in $languageName language.
                    
                    Provide your response as a JSON object with this EXACT structure:
                    {
                        "name": "Recipe Name in $languageName",
                        "time": "cooking time in $languageName (e.g., '30 minutes' or '30 minutos' or '30 minutes')",
                        "reason": "Why this recipe suits the user's mood in $languageName (2-3 sentences)",
                        "ingredients": ["ingredient 1 in $languageName", "ingredient 2 in $languageName", ...],
                        "steps": ["step 1 in $languageName", "step 2 in $languageName", ...],
                        "image_url": "https://images.unsplash.com/photo-suitable-for-recipe?w=800"
                    }
                    
                    Rules:
                    - Recipe name should be creative and appealing in $languageName
                    - Use primarily the provided ingredients
                    - Match recipe complexity to available time
                    - Reason should explain how the recipe matches the mood in $languageName
                    - For image_url, use a relevant Unsplash food photo URL
                    - All text fields (name, reason, ingredients, steps, time) MUST be in $languageName
                    - Ensure valid JSON format
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonResponse = response.text
                    ?.trim()
                    ?.removePrefix("```json")
                    ?.removeSuffix("```")
                    ?.trim()

                if (jsonResponse.isNullOrBlank()) {
                    _uiState.value = RecipeFinderState.Error("Empty response from AI")
                    return@launch
                }

                val recipe = Gson().fromJson(jsonResponse, GeneratedRecipe::class.java)
                val recipeId = generateRecipeId(recipe)

                _uiState.value = RecipeFinderState.Success(recipe, recipeId)

                checkIfFavorite(recipeId)
                checkIfSaved(recipeId)

            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "Error generating recipe", e)
                _uiState.value = RecipeFinderState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun generateRecipeId(recipe: GeneratedRecipe): String {
        val input = "${recipe.name}${recipe.ingredients.joinToString("")}"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun checkIfFavorite(recipeId: String) {
        _isFavorite.value = firebaseRepository.isFavorite(recipeId)
    }

    private suspend fun checkIfSaved(recipeId: String) {
        val allRecipes = firebaseRepository.getAllRecipes()
        _isSaved.value = allRecipes.getOrNull()?.any { it.id == recipeId } == true
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (state !is RecipeFinderState.Success) {
            Log.e("RecipeFinderVM", "Cannot save - state is not Success: $state")
            return
        }

        if (_isSaved.value) {
            Log.d("RecipeFinderVM", "Recipe already saved, skipping")
            return
        }

        val recipeId = state.recipeId
        val recipe = state.recipe

        Log.d("RecipeFinderVM", "=== SAVE RECIPE BUTTON CLICKED ===")
        Log.d("RecipeFinderVM", "Recipe ID: $recipeId")
        Log.d("RecipeFinderVM", "Recipe Name: ${recipe.name}")
        Log.d("RecipeFinderVM", "Current isFavorite status: ${_isFavorite.value}")

        viewModelScope.launch(Dispatchers.IO) {
            _saveInProgress.value = true
            try {
                val firebaseRecipe = FirebaseRecipe(
                    id = recipeId,
                    name = recipe.name,
                    time = recipe.time,
                    ingredients = recipe.ingredients,
                    steps = recipe.steps,
                    imageUrl = recipe.image_url,
                    reason = recipe.reason,
                    isFavorite = _isFavorite.value
                )

                Log.d("RecipeFinderVM", "Calling firebaseRepository.saveRecipe()...")
                val result = firebaseRepository.saveRecipe(firebaseRecipe)

                if (result.isSuccess) {
                    _isSaved.value = true
                    Log.d("RecipeFinderVM", " Recipe saved successfully! isSaved = true")
                } else {
                    Log.e("RecipeFinderVM", " Error saving recipe: ${result.exceptionOrNull()}")
                }

            } catch (e: Exception) {
                Log.e("RecipeFinderVM", " Exception saving recipe", e)
            } finally {
                _saveInProgress.value = false
                Log.d("RecipeFinderVM", "Save operation completed. saveInProgress = false")
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        if (state !is RecipeFinderState.Success) return

        val recipeId = state.recipeId
        val recipe = state.recipe
        val newFavoriteStatus = !_isFavorite.value

        viewModelScope.launch(Dispatchers.IO) {
            _favoriteInProgress.value = true
            try {
                val firebaseRecipe = FirebaseRecipe(
                    id = recipeId,
                    name = recipe.name,
                    time = recipe.time,
                    ingredients = recipe.ingredients,
                    steps = recipe.steps,
                    imageUrl = recipe.image_url,
                    reason = recipe.reason,
                    isFavorite = newFavoriteStatus
                )

                val result = firebaseRepository.toggleFavorite(firebaseRecipe, newFavoriteStatus)

                if (result.isSuccess) {
                    _isFavorite.value = newFavoriteStatus
                    if (newFavoriteStatus) {
                        _isSaved.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "Error toggling favorite", e)
            } finally {
                _favoriteInProgress.value = false
            }
        }
    }

    fun addMealToToday(recipeId: String, calories: Int, protein: Int, carbs: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nutritionRepo = NutritionRepository()
                nutritionRepo.addMealToToday(recipeId, calories, protein, carbs)
                Log.d("RecipeFinderVM", " Meal added to today: $calories cal, $protein g protein, $carbs g carbs")
            } catch (e: Exception) {
                Log.e("RecipeFinderVM", " Error adding meal to today", e)
            }
        }
    }
}