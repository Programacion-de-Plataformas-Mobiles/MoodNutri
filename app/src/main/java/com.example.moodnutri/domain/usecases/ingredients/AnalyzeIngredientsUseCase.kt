package com.example.moodnutri.domain.usecases.ingredients

import android.graphics.Bitmap
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.domain.usecases.UseCase
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.firstOrNull

// ============================================
// AnalyzeIngredientsUseCase.kt
// ============================================
class AnalyzeIngredientsUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<Bitmap, Result<List<String>>> {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override suspend fun invoke(params: Bitmap): Result<List<String>> {
        return try {
            val language = preferencesManager.language.firstOrNull() ?: "en"
            val languageName = when(language) {
                "es" -> "Spanish"
                "fr" -> "French"
                else -> "English"
            }

            val prompt = """
                Analyze this image and identify all visible food ingredients.
                
                IMPORTANT: Respond in $languageName language.
                
                Return ONLY a JSON array of ingredient names in $languageName.
                Format: ["ingredient 1 in $languageName", "ingredient 2 in $languageName", ...]
                
                Do not include quantities, just ingredient names.
                Do not include any other text or markdown formatting.
            """.trimIndent()

            val inputContent = content {
                image(params)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)

            val jsonString = response.text
                ?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```")
                ?: "[]"

            val itemType = object : TypeToken<List<String>>() {}.type
            val ingredients: List<String> = Gson().fromJson(jsonString, itemType)

            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}