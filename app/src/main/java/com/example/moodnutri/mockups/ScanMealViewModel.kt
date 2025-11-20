package com.example.moodnutri.mockups

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openAi.GeneratedRecipe
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanMealUiState {
    object Idle : ScanMealUiState
    object Loading : ScanMealUiState
    object Success : ScanMealUiState 
    data class Error(val message: String) : ScanMealUiState
}

class ScanMealViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScanMealUiState>(ScanMealUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)
    
    // Estado para el resultado del cálculo de calorías
    val totalCalories = mutableStateOf<String?>(null)
    val isCalculatingCalories = mutableStateOf(false)

    val detectedIngredients = mutableStateListOf<MealIngredient>()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun analyzeMeal(bitmap: Bitmap, baseRecipe: GeneratedRecipe? = null) {
        selectedImage.value = bitmap
        _uiState.value = ScanMealUiState.Loading
        detectedIngredients.clear()
        totalCalories.value = null // Resetear calorías al analizar nueva imagen

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(baseRecipe)

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

    private fun buildPrompt(baseRecipe: GeneratedRecipe?): String {
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

        You must respond ONLY with a valid JSON array of objects. Each object must have two fields:
        - "name": The name of the ingredient.
        - "quantity": The estimated quantity (e.g., "100g", "1 cup", "2 slices").

        Example format:
        [
            {"name": "Chicken Breast", "quantity": "150g"},
            {"name": "Broccoli", "quantity": "80g"}
        ]
        
        Do not include any other text or markdown formatting. Just the JSON array.
        """
    }

    fun calculateCalories() {
        if (detectedIngredients.isEmpty()) return
        
        isCalculatingCalories.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ingredientsList = detectedIngredients.joinToString("\n") { "- ${it.name}: ${it.quantity}" }
                val prompt = """
                    Calculate the total calories for the following list of ingredients with their quantities:
                    
                    $ingredientsList
                    
                    Please provide ONLY the total numeric value of calories (e.g., "540"). Do not add any text or explanation. Just the number.
                    If you cannot determine exactly, provide your best estimate.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val calories = response.text?.trim() ?: "N/A"
                
                totalCalories.value = calories
            } catch (e: Exception) {
                e.printStackTrace()
                totalCalories.value = "Error"
            } finally {
                isCalculatingCalories.value = false
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
            // Mantenemos el ID original para evitar recomposiciones innecesarias
            val originalId = detectedIngredients[index].id
            detectedIngredients[index] = MealIngredient(newName, newQuantity, id = originalId)
        }
    }

    fun clearScan() {
        selectedImage.value = null
        detectedIngredients.clear()
        totalCalories.value = null
        _uiState.value = ScanMealUiState.Idle
    }
}
