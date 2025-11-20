package com.example.moodnutri.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.RecipeRepository
import com.example.moodnutri.data.local.AppDatabase
import com.example.moodnutri.data.models.openAi.ChatMessage
import com.example.moodnutri.data.models.openAi.ChatRequest
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import com.example.moodnutri.data.repository.LocalRecipeRepository
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.data.network.RetrofitInstance
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
    private val localRepository = LocalRecipeRepository(
        AppDatabase.getDatabase(application).favoriteRecipeDao()
    )
    private val preferencesManager = UserPreferencesManager(application)
    private val recipeRepository = RecipeRepository()
    private val openAiService = RetrofitInstance.openAiApi

    // Caché para la última búsqueda exitosa
    private var lastSearchedIngredients: List<String>? = null
    private var lastSearchedMood: String? = null
    private var lastSearchedTime: String? = null

    fun searchRecipe(userIngredients: List<String>, mood: String, availableTime: String) {
        // Si la búsqueda es idéntica a la anterior exitosa, no hacer nada.
        if (_uiState.value is RecipeFinderState.Success &&
            lastSearchedIngredients == userIngredients &&
            lastSearchedMood == mood &&
            lastSearchedTime == availableTime) {
            return
        }

        _uiState.value = RecipeFinderState.Loading
        _isFavorite.value = false
        _isSaved.value = false

        // Guardar los parámetros de esta búsqueda
        lastSearchedIngredients = userIngredients
        lastSearchedMood = mood
        lastSearchedTime = availableTime

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("RecipeFinderVM", "🔍 INICIANDO BÚSQUEDA DE RECETAS...")
                Log.d("RecipeFinderVM", "📋 Ingredientes originales: $userIngredients")

                // Primero traducir los ingredientes a inglés usando ChatGPT
                val englishIngredients = translateIngredientsToEnglish(userIngredients)

                if (englishIngredients.isNotEmpty()) {
                    Log.d("RecipeFinderVM", "🌐 Ingredientes traducidos: $englishIngredients")

                    // Buscar recetas reales en MealDB con ingredientes en inglés
                    Log.d("RecipeFinderVM", "🔄 Buscando recetas en MealDB con ingredientes en inglés...")
                    val realRecipes = recipeRepository.findRecipesByIngredients(englishIngredients)

                    if (realRecipes.isNotEmpty()) {
                        Log.d("RecipeFinderVM", "✅ ENCONTRADAS ${realRecipes.size} recetas en MealDB")
                        // Si encontramos recetas reales, generar la receta final
                        generateRecipeWithRealRecipes(userIngredients, englishIngredients, mood, availableTime, realRecipes)
                    } else {
                        Log.d("RecipeFinderVM", "❌ NO se encontraron recetas en MealDB")
                        _uiState.value = RecipeFinderState.Error("No recipes found with your ingredients. Please try different ingredients.")
                    }
                } else {
                    Log.d("RecipeFinderVM", "❌ Error traduciendo ingredientes")
                    _uiState.value = RecipeFinderState.Error("Error processing ingredients. Please try again.")
                }

            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "❌ Error en búsqueda principal", e)
                _uiState.value = RecipeFinderState.Error("Error searching for recipes. Please check your connection and try again.")
            }
        }
    }

    private suspend fun translateIngredientsToEnglish(ingredients: List<String>): List<String> {
        return try {
            Log.d("RecipeFinderVM", "🤖 Traduciendo ingredientes a inglés con ChatGPT...")

            val prompt = """
                Translate the following list of food ingredients to English. 
                Return ONLY a JSON array with the English translations, nothing else.
                
                Ingredients to translate: ${ingredients.joinToString(", ")}
                
                Example response: ["chicken", "rice", "tomato"]
                
                Rules:
                - Return only a JSON array
                - No additional text or explanations
                - Use common food ingredient names in English
                - Keep the same order as the input list
            """.trimIndent()

            val chatRequest = ChatRequest(
                model = "gpt-4",
                messages = listOf(
                    ChatMessage("system", "You are a translation assistant. Respond with ONLY a JSON array of translated ingredients."),
                    ChatMessage("user", prompt)
                )
            )

            val chatResponse = openAiService.getRecipeSuggestion(request = chatRequest)
            val jsonResponse = chatResponse.choices.firstOrNull()?.message?.content

            if (jsonResponse.isNullOrBlank()) {
                Log.e("RecipeFinderVM", "❌ Respuesta vacía en traducción")
                return emptyList()
            }

            val cleanJsonResponse = jsonResponse
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            Log.d("RecipeFinderVM", "📨 Respuesta de traducción: $cleanJsonResponse")

            val translatedIngredients = Gson().fromJson(cleanJsonResponse, Array<String>::class.java).toList()
            Log.d("RecipeFinderVM", "✅ Ingredientes traducidos exitosamente")
            translatedIngredients

        } catch (e: Exception) {
            Log.e("RecipeFinderVM", "❌ Error en traducción", e)
            emptyList()
        }
    }

    private suspend fun generateRecipeWithRealRecipes(
        userIngredients: List<String>,
        englishIngredients: List<String>,
        mood: String,
        availableTime: String,
        realRecipes: List<com.example.moodnutri.data.models.theMealDb.MealDetails>
    ) {
        try {
            // Obtener el idioma actual
            val language = preferencesManager.language.firstOrNull() ?: "en"
            val languageName = when(language) {
                "es" -> "Spanish"
                "fr" -> "French"
                else -> "English"
            }

            Log.d("RecipeFinderVM", "🌍 Idioma seleccionado: $languageName")
            Log.d("RecipeFinderVM", "📊 Procesando ${realRecipes.size} recetas reales")

            // Convertir recetas reales a texto para el prompt
            val recipesAsText = realRecipes.joinToString("\n---\n") { recipe ->
                "Name: ${recipe.strMeal}\n" +
                        "ImageURL: ${recipe.strMealThumb}\n" +
                        "Category: ${recipe.strCategory}\n" +
                        "Area: ${recipe.strArea}\n" +
                        "Instructions: ${recipe.strInstructions}\n" +
                        "Ingredients: ${recipe.getIngredientsWithMeasures().joinToString()}"
            }

            Log.d("RecipeFinderVM", "📨 Enviando prompt a ChatGPT para generar receta...")

            val prompt = """
                User's context:
                - Mood: $mood
                - Available Time: $availableTime
                - My Ingredients: ${userIngredients.joinToString(", ")} (original language)
                - English Ingredients: ${englishIngredients.joinToString(", ")} (for reference)

                Here is a list of real recipes I found:
                $recipesAsText

                Based ONLY on the provided recipes, please select ONE recipe that best fits my context. 
                **The available time is the most important factor.** The recipe's total time should be less than or equal to my available time.
                Do not invent anything. If no recipe is a good fit, you can choose the one that is closest and mention why.

                IMPORTANT: Generate the entire recipe in $languageName language.

                Your response MUST be a single, minified JSON object with the following exact structure and nothing else:
                {
                    "name": "Recipe Name in $languageName",
                    "time": "cooking time in $languageName (e.g., '30 minutes' or '30 minutos' or '30 minutes')",
                    "reason": "Why this recipe suits the user's mood in $languageName (2-3 sentences)",
                    "ingredients": ["ingredient 1 in $languageName", "ingredient 2 in $languageName", ...],
                    "steps": ["step 1 in $languageName", "step 2 in $languageName", ...],
                    "image_url": "USE_THE_ACTUAL_IMAGE_URL_FROM_THE_SELECTED_RECIPE"
                }

                CRITICAL INSTRUCTIONS:
                - You MUST use the exact ImageURL from the real recipe you selected/adapted
                - Do NOT use Unsplash or any other image source
                - Use the actual strMealThumb URL from the recipe data above
                - Do NOT invent new ingredients or steps - only use what's in the provided recipes
                - If adapting a recipe, only make minor adjustments to fit time constraints
                - All text in the response (name, reason, ingredients, steps) MUST be in $languageName

                In the 'reason' field, briefly explain why you chose this recipe based on my mood, time, and ingredients.
            """.trimIndent()

            val chatRequest = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage("system", "You are a helpful chef assistant. You must strictly respond with a JSON object. Use ONLY the provided recipes - do not invent anything."),
                    ChatMessage("user", prompt)
                )
            )

            val chatResponse = openAiService.getRecipeSuggestion(request = chatRequest)
            val jsonResponse = chatResponse.choices.firstOrNull()?.message?.content

            if (jsonResponse.isNullOrBlank()) {
                Log.w("RecipeFinderVM", "⚠️ Respuesta vacía de ChatGPT")
                _uiState.value = RecipeFinderState.Error("No response from AI. Please try again.")
                return
            }

            Log.d("RecipeFinderVM", "✅ Respuesta recibida de ChatGPT")
            val cleanJsonResponse = cleanJsonResponse(jsonResponse)
            val recipe = Gson().fromJson(cleanJsonResponse, GeneratedRecipe::class.java)
            val recipeId = generateRecipeId(recipe)

            Log.d("RecipeFinderVM", "🎉 RECETA CREADA CON MEALDB")
            Log.d("RecipeFinderVM", "📛 Nombre: ${recipe.name}")
            Log.d("RecipeFinderVM", "⏱️ Tiempo: ${recipe.time}")
            Log.d("RecipeFinderVM", "🖼️ Imagen: ${recipe.image_url}")

            _uiState.value = RecipeFinderState.Success(recipe, recipeId)
            checkIfFavorite(recipeId)
            checkIfSaved(recipeId)

        } catch (e: Exception) {
            Log.e("RecipeFinderVM", "❌ Error generando receta", e)
            _uiState.value = RecipeFinderState.Error("Error generating recipe. Please try again.")
        }
    }

    private fun cleanJsonResponse(jsonResponse: String): String {
        return jsonResponse
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun generateRecipeId(recipe: GeneratedRecipe): String {
        val input = "${recipe.name}${recipe.ingredients.joinToString("")}${recipe.steps.joinToString("")}"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun checkIfFavorite(recipeId: String) {
        try {
            val isFirebaseFavorite = firebaseRepository.isFavorite(recipeId)
            val isLocalFavorite = localRepository.isFavorite(recipeId)
            _isFavorite.value = isFirebaseFavorite || isLocalFavorite
            Log.d("RecipeFinderVM", "✅ Favorite status checked - Firebase: $isFirebaseFavorite, Local: $isLocalFavorite")
        } catch (e: Exception) {
            Log.e("RecipeFinderVM", "Error checking favorite status", e)
            _isFavorite.value = false
        }
    }

    private suspend fun checkIfSaved(recipeId: String) {
        try {
            val allRecipes = firebaseRepository.getAllRecipes()
            _isSaved.value = allRecipes.getOrNull()?.any { it.id == recipeId } == true
            Log.d("RecipeFinderVM", "✅ Saved status checked: ${_isSaved.value}")
        } catch (e: Exception) {
            Log.e("RecipeFinderVM", "Error checking saved status", e)
            _isSaved.value = false
        }
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
                    Log.d("RecipeFinderVM", "✅ Recipe saved successfully! isSaved = true")
                } else {
                    Log.e("RecipeFinderVM", "❌ Error saving recipe: ${result.exceptionOrNull()}")
                }

            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "❌ Exception saving recipe", e)
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

        Log.d("RecipeFinderVM", "=== TOGGLE FAVORITE ===")
        Log.d("RecipeFinderVM", "Recipe ID: $recipeId")
        Log.d("RecipeFinderVM", "New favorite status: $newFavoriteStatus")

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

                // Guardar/actualizar en Firebase
                val firebaseResult = firebaseRepository.toggleFavorite(firebaseRecipe, newFavoriteStatus)

                if (firebaseResult.isSuccess) {
                    // Si se marcó como favorito, también guardar en Room local
                    if (newFavoriteStatus) {
                        Log.d("RecipeFinderVM", "Agregando a favoritos locales (Room)...")
                        localRepository.addToFavorites(recipe, recipeId)
                        _isSaved.value = true
                        Log.d("RecipeFinderVM", "✅ Receta agregada a favoritos locales")
                    } else {
                        // Si se quitó de favoritos, remover de Room
                        Log.d("RecipeFinderVM", "Removiendo de favoritos locales (Room)...")
                        localRepository.removeFromFavorites(recipeId)
                        Log.d("RecipeFinderVM", "✅ Receta removida de favoritos locales")
                    }

                    _isFavorite.value = newFavoriteStatus
                    Log.d("RecipeFinderVM", "✅ Toggle favorite successful")
                } else {
                    Log.e("RecipeFinderVM", "❌ Error toggling favorite in Firebase")
                }
            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "❌ Error toggling favorite", e)
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
                Log.d("RecipeFinderVM", "✅ Meal added to today: $calories cal, $protein g protein, $carbs g carbs")
            } catch (e: Exception) {
                Log.e("RecipeFinderVM", "❌ Error adding meal to today", e)
            }
        }
    }
}