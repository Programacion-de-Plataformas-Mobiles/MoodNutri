package com.example.moodnutri.domain.usecases.meal

import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.domain.usecases.UseCase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson

// ============================================
// CalculateNutritionUseCase.kt
// ============================================
data class NutritionInfo(
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0
)

class CalculateNutritionUseCase : UseCase<List<MealIngredient>, Result<NutritionInfo>> {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun invoke(params: List<MealIngredient>): Result<NutritionInfo> {
        return try {
            val ingredientsList = params.joinToString("\n") { "- ${it.name}: ${it.quantity}" }

            val prompt = """
                Calculate the total nutritional information for the following ingredients:
                
                $ingredientsList
                
                Please provide ONLY a JSON object:
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
            val jsonResponse = response.text
                ?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```") ?: "{}"

            val nutrition = Gson().fromJson(jsonResponse, NutritionInfo::class.java)
            Result.success(nutrition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}