package com.example.moodnutri.domain.usecases.meal

import android.graphics.Bitmap
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.domain.usecases.UseCase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull

// ============================================
// AnalyzeMealUseCase.kt
// ============================================
data class AnalyzeMealParams(
    val bitmap: Bitmap,
    val baseRecipe: GeneratedRecipe? = null
)

class AnalyzeMealUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<AnalyzeMealParams, Result<List<MealIngredient>>> {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun invoke(params: AnalyzeMealParams): Result<List<MealIngredient>> {
        return try {
            val language = preferencesManager.language.firstOrNull() ?: "en"
            val languageName = when(language) {
                "es" -> "Spanish"
                "fr" -> "French"
                else -> "English"
            }

            val prompt = buildPrompt(params.baseRecipe, languageName)

            val inputContent = content {
                image(params.bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)

            val jsonString = response.text
                ?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```") ?: "[]"

            val itemType = object : TypeToken<List<MealIngredient>>() {}.type
            val ingredients: List<MealIngredient> = Gson().fromJson(jsonString, itemType)

            val ingredientsWithIds = ingredients.map {
                it.copy(id = java.util.UUID.randomUUID().toString())
            }

            Result.success(ingredientsWithIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(baseRecipe: GeneratedRecipe?, languageName: String): String {
        val baseInstruction = if (baseRecipe != null) {
            """
            I have cooked a meal based on this recipe:
            Name: ${baseRecipe.name}
            Expected Ingredients: ${baseRecipe.ingredients.joinToString(", ")}
            
            Please analyze the image of the cooked meal. Compare it with the expected recipe.
            Identify the visible ingredients and estimate their quantity.
            """
        } else {
            """
            Please analyze the image of the meal. Identify all visible ingredients and estimate their quantity.
            """
        }

        return """
        $baseInstruction

        IMPORTANT: Respond in $languageName language.

        You must respond ONLY with a valid JSON array of objects. Each object must have:
        - "name": The name of the ingredient in $languageName.
        - "quantity": The estimated quantity in $languageName.

        Example format:
        [
            {"name": "Ingredient name in $languageName", "quantity": "quantity in $languageName"},
            {"name": "Another ingredient in $languageName", "quantity": "quantity in $languageName"}
        ]
        """
    }
}