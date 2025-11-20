package com.example.moodnutri.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.RecipeDao
import com.example.moodnutri.data.RecipeRepository
import com.example.moodnutri.data.models.openAi.ChatMessage
import com.example.moodnutri.data.models.openAi.ChatRequest
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.network.OpenAiApiService
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecipeFinderState {
    object Idle : RecipeFinderState
    object Loading : RecipeFinderState
    data class Success(val recipe: GeneratedRecipe) : RecipeFinderState
    data class Error(val message: String) : RecipeFinderState
}

@HiltViewModel
class RecipeFinderViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeRepository: RecipeRepository,
    private val openAiService: OpenAiApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeFinderState>(RecipeFinderState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    // Caché para la última búsqueda exitosa
    private var lastSearchedIngredients: List<String>? = null
    private var lastSearchedMood: String? = null
    private var lastSearchedTime: String? = null

    fun searchRecipe(
        userIngredients: List<String>,
        mood: String,
        availableTime: String
    ) {
        // Si la búsqueda es idéntica a la anterior exitosa, no hacer nada.
        if (_uiState.value is RecipeFinderState.Success &&
            lastSearchedIngredients == userIngredients &&
            lastSearchedMood == mood &&
            lastSearchedTime == availableTime) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RecipeFinderState.Loading

            // Guardar los parámetros de esta búsqueda
            lastSearchedIngredients = userIngredients
            lastSearchedMood = mood
            lastSearchedTime = availableTime

            try {
                val realRecipes = recipeRepository.findRecipesByIngredients(userIngredients)
                if (realRecipes.isEmpty()) {
                    _uiState.value = RecipeFinderState.Error("No recipes found with your ingredients.")
                    return@launch
                }

                val prompt = buildPrompt(mood, availableTime, userIngredients, realRecipes)
                val chatRequest = ChatRequest(
                    model = "gpt-4.1",
                    messages = listOf(
                        ChatMessage("system", "You are a helpful chef assistant. You must strictly respond with a JSON object."),
                        ChatMessage("user", prompt)
                    )
                )

                val chatResponse = openAiService.getRecipeSuggestion(request = chatRequest)
                val jsonResponse = chatResponse.choices.firstOrNull()?.message?.content

                if (jsonResponse == null) {
                    _uiState.value = RecipeFinderState.Error("ChatGPT did not return a valid response.")
                    return@launch
                }

                val finalRecipe = Gson().fromJson(jsonResponse, GeneratedRecipe::class.java)
                _uiState.value = RecipeFinderState.Success(finalRecipe)
                recipeRepository.saveRecipe(finalRecipe)

                // LÍNEA NUEVA AÑADIDA: Guardar receta reciente en Firebase
                recipeRepository.saveRecentRecipe(finalRecipe)

                checkIfFavorite(finalRecipe.name)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = RecipeFinderState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private fun buildPrompt(
        mood: String,
        availableTime: String,
        userIngredients: List<String>,
        recipesFromApi: List<com.example.moodnutri.data.models.theMealDb.MealDetails>
    ): String {
        val recipesAsText = recipesFromApi.joinToString("\n---\n") { recipe ->
            "Name: ${recipe.strMeal}\n" +
                    "ImageURL: ${recipe.strMealThumb}\n" +
                    "Category: ${recipe.strCategory}\n" +
                    "Area: ${recipe.strArea}\n" +
                    "Instructions: ${recipe.strInstructions}\n" +
                    "Ingredients: ${recipe.getIngredientsWithMeasures().joinToString()}"
        }

        return """
        User's context:
        - Mood: $mood
        - Available Time: $availableTime
        - My Ingredients: ${userIngredients.joinToString(", ")}

        Here is a list of real recipes I found:
        $recipesAsText

        Based ONLY on the provided recipes, please select or adapt ONE recipe that best fits my context. 
        **The available time is the most important factor.** The recipe's total time should be less than or equal to my available time.
        Do not invent anything. If no recipe is a good fit, you can choose the one that is closest and mention why.

        Your response MUST be a single, minified JSON object with the following exact structure and nothing else:
        {"name":"", "time":"", "ingredients":[], "steps":[], "image_url":"", "reason":""}

        In the 'reason' field, briefly explain why you chose this recipe based on my mood, time, and ingredients.
        """
    }

    fun addOrRemoveFromFavorites(recipe: GeneratedRecipe, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                if (recipeDao.getFavoriteRecipesCount() < 5) {
                    val recipeToSave = com.example.moodnutri.data.Recipe(recipe.name, recipe.toString(), true)
                    recipeDao.insert(recipeToSave)
                    recipeRepository.saveFavorite(recipe)
                    _isFavorite.value = true
                }
            } else {
                recipeDao.delete(recipe.name)
                recipeRepository.removeFavorite(recipe.name)
                _isFavorite.value = false
            }
        }
    }

    private fun checkIfFavorite(recipeName: String) {
        viewModelScope.launch {
            val favoriteRecipes = recipeDao.getFavoriteRecipes()
            _isFavorite.value = favoriteRecipes.any { it.name == recipeName }
        }
    }
}