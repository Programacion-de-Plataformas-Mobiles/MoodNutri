package com.example.moodnutri.mockups

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface ScanMealUiState {
    object Idle : ScanMealUiState
    object Loading : ScanMealUiState
    object Success : ScanMealUiState
    data class Error(val message: String) : ScanMealUiState
}

data class NutritionInfo(
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0
)

class ScanMealViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ScanMealUiState>(ScanMealUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val _nutritionInfo = MutableStateFlow(NutritionInfo())
    val nutritionInfo = _nutritionInfo.asStateFlow()

    val isCalculatingNutrition = mutableStateOf(false)

    val detectedIngredients = mutableStateListOf<MealIngredient>()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val nutritionRepository = NutritionRepository()
    private val preferencesManager = UserPreferencesManager(application)

    fun analyzeMeal(bitmap: Bitmap, baseRecipe: GeneratedRecipe? = null) {
        selectedImage.value = bitmap
        _uiState.value = ScanMealUiState.Loading
        detectedIngredients.clear()
        _nutritionInfo.value = NutritionInfo()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val language = preferencesManager.language.firstOrNull() ?: "en"
                val languageName = when(language) {
                    "es" -> "Spanish"
                    "fr" -> "French"
                    else -> "English"
                }

                val prompt = buildPrompt(baseRecipe, languageName)

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)

                val jsonString = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```") ?: "[]"

                val itemType = object : TypeToken<List<MealIngredient>>() {}.type
                val ingredients: List<MealIngredient> = Gson().fromJson(jsonString, itemType)

                val ingredientsWithIds = ingredients.map {
                    it.copy(id = java.util.UUID.randomUUID().toString())
                }

                detectedIngredients.addAll(ingredientsWithIds)
                _uiState.value = ScanMealUiState.Success

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ScanMealUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun buildPrompt(baseRecipe: GeneratedRecipe?, languageName: String): String {
        val baseInstruction = if (baseRecipe != null) {
            """
            I have cooked a meal based on this recipe:
            Name: ${baseRecipe.name}
            Expected Ingredients: ${baseRecipe.ingredients.joinToString(", ")}
            
            Please analyze the image of the cooked meal. Compare it with the expected recipe.
            Identify the visible ingredients and estimate their quantity as accurately as possible based on the image.
            Use the recipe quantities as a baseline but adjust if the image shows differently.
            """
        } else {
            """
            Please analyze the image of the meal. Identify all visible ingredients and estimate their quantity as accurately as possible.
            """
        }

        return """
        $baseInstruction

        IMPORTANT: Respond in $languageName language.

        You must respond ONLY with a valid JSON array of objects. Each object must have two fields:
        - "name": The name of the ingredient in $languageName.
        - "quantity": The estimated quantity in $languageName (e.g., "100g", "1 taza", "2 tranches").

        Example format for $languageName:
        [
            {"name": "Ingredient name in $languageName", "quantity": "quantity in $languageName"},
            {"name": "Another ingredient in $languageName", "quantity": "quantity in $languageName"}
        ]
        
        Do not include any other text or markdown formatting. Just the JSON array.
        """
    }

    fun calculateNutrition() {
        if (detectedIngredients.isEmpty()) return

        isCalculatingNutrition.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ingredientsList = detectedIngredients.joinToString("\n") { "- ${it.name}: ${it.quantity}" }
                val prompt = """
                    Calculate the total nutritional information for the following list of ingredients with their quantities:
                    
                    $ingredientsList
                    
                    Please provide ONLY a JSON object with the following structure:
                    {
                        "calories": 540,
                        "protein": 45,
                        "carbs": 35
                    }
                    
                    Where:
                    - calories is the total calories in kcal
                    - protein is the total protein in grams
                    - carbs is the total carbohydrates in grams
                    
                    Respond ONLY with the JSON object, no other text.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val jsonResponse = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```") ?: "{}"

                val nutrition = Gson().fromJson(jsonResponse, NutritionInfo::class.java)
                _nutritionInfo.value = nutrition

            } catch (e: Exception) {
                e.printStackTrace()
                _nutritionInfo.value = NutritionInfo(calories = 0, protein = 0, carbs = 0)
            } finally {
                isCalculatingNutrition.value = false
            }
        }
    }

    fun addMealToToday() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nutrition = _nutritionInfo.value
                if (nutrition.calories > 0) {
                    val result = nutritionRepository.addMealToToday(
                        recipeId = "scanned_meal_${System.currentTimeMillis()}",
                        calories = nutrition.calories,
                        protein = nutrition.protein,
                        carbs = nutrition.carbs
                    )

                    if (result.isSuccess) {
                        android.util.Log.d("ScanMealVM", " Meal added to today successfully")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScanMealVM", " Error adding meal to today", e)
            }
        }
    }

    fun addIngredient(name: String, quantity: String) {
        if (name.isNotBlank()) {
            detectedIngredients.add(MealIngredient(name, quantity))
        }
    }

    fun removeIngredient(ingredient: MealIngredient) {
        detectedIngredients.remove(ingredient)
    }

    fun updateIngredient(index: Int, newName: String, newQuantity: String) {
        if (index in detectedIngredients.indices) {
            val originalId = detectedIngredients[index].id
            detectedIngredients[index] = MealIngredient(newName, newQuantity, id = originalId)
        }
    }

    fun clearScan() {
        selectedImage.value = null
        detectedIngredients.clear()
        _nutritionInfo.value = NutritionInfo()
        _uiState.value = ScanMealUiState.Idle
    }
}