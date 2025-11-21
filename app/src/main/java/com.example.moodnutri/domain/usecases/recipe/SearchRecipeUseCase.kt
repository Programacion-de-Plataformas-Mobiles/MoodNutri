package com.example.moodnutri.domain.usecases.recipe

import android.util.Log
import com.example.moodnutri.data.models.openai.ChatMessage
import com.example.moodnutri.data.models.openai.ChatRequest
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.models.themealdb.MealDetails
import com.example.moodnutri.data.remote.api.OpenAiApiService
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.RecipeRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest

// ============================================
// SearchRecipeUseCase.kt
// ============================================
data class SearchRecipeParams(
    val userIngredients: List<String>,
    val mood: String,
    val availableTime: String
)

data class RecipeSearchResult(
    val recipe: GeneratedRecipe,
    val recipeId: String
)

class SearchRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val openAiService: OpenAiApiService,
    private val preferencesManager: UserPreferencesManager
) : UseCase<SearchRecipeParams, Result<RecipeSearchResult>> {

    override suspend fun invoke(params: SearchRecipeParams): Result<RecipeSearchResult> {
        return try {
            Log.d("SearchRecipeUseCase", "Starting recipe search...")

            // 1. Translate ingredients to English
            val englishIngredients = translateIngredientsToEnglish(params.userIngredients)

            if (englishIngredients.isEmpty()) {
                return Result.failure(Exception("Error processing ingredients"))
            }

            // 2. Search recipes in MealDB
            val realRecipes = recipeRepository.findRecipesByIngredients(englishIngredients)

            if (realRecipes.isEmpty()) {
                return Result.failure(Exception("No recipes found with your ingredients"))
            }

            // 3. Generate recipe with AI
            val recipe = generateRecipeWithRealRecipes(
                params.userIngredients,
                englishIngredients,
                params.mood,
                params.availableTime,
                realRecipes
            )

            val recipeId = generateRecipeId(recipe)
            Result.success(RecipeSearchResult(recipe, recipeId))

        } catch (e: Exception) {
            Log.e("SearchRecipeUseCase", "Error searching recipe", e)
            Result.failure(e)
        }
    }

    private suspend fun translateIngredientsToEnglish(ingredients: List<String>): List<String> {
        return try {
            val prompt = """
                Translate the following list of food ingredients to English. 
                Return ONLY a JSON array with the English translations, nothing else.
                
                Ingredients to translate: ${ingredients.joinToString(", ")}
                
                Example response: ["chicken", "rice", "tomato"]
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
                ?.trim()
                ?.removePrefix("```json")
                ?.removePrefix("```")
                ?.removeSuffix("```")
                ?.trim() ?: return emptyList()

            Gson().fromJson(jsonResponse, Array<String>::class.java).toList()
        } catch (e: Exception) {
            Log.e("SearchRecipeUseCase", "Error translating ingredients", e)
            emptyList()
        }
    }

    private suspend fun generateRecipeWithRealRecipes(
        userIngredients: List<String>,
        englishIngredients: List<String>,
        mood: String,
        availableTime: String,
        realRecipes: List<MealDetails>
    ): GeneratedRecipe {
        val language = preferencesManager.language.firstOrNull() ?: "en"
        val languageName = when(language) {
            "es" -> "Spanish"
            "fr" -> "French"
            else -> "English"
        }

        val recipesAsText = realRecipes.joinToString("\n---\n") { recipe ->
            "Name: ${recipe.strMeal}\n" +
                    "ImageURL: ${recipe.strMealThumb}\n" +
                    "Category: ${recipe.strCategory}\n" +
                    "Area: ${recipe.strArea}\n" +
                    "Instructions: ${recipe.strInstructions}\n" +
                    "Ingredients: ${recipe.getIngredientsWithMeasures().joinToString()}"
        }

        val prompt = """
            User's context:
            - Mood: $mood
            - Available Time: $availableTime
            - My Ingredients: ${userIngredients.joinToString(", ")}
            - English Ingredients: ${englishIngredients.joinToString(", ")}

            Here is a list of real recipes I found:
            $recipesAsText

            Based ONLY on the provided recipes, please select ONE recipe that best fits my context. 
            The available time is the most important factor.
            
            IMPORTANT: Generate the entire recipe in $languageName language.

            Your response MUST be a single JSON object:
            {
                "name": "Recipe Name in $languageName",
                "time": "cooking time in $languageName",
                "reason": "Why this recipe suits the user's mood in $languageName",
                "ingredients": ["ingredient 1 in $languageName", "ingredient 2 in $languageName"],
                "steps": ["step 1 in $languageName", "step 2 in $languageName"],
                "image_url": "USE_THE_ACTUAL_IMAGE_URL_FROM_THE_SELECTED_RECIPE"
            }
        """.trimIndent()

        val chatRequest = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                ChatMessage("system", "You are a helpful chef assistant. Respond with a JSON object only."),
                ChatMessage("user", prompt)
            )
        )

        val chatResponse = openAiService.getRecipeSuggestion(request = chatRequest)
        val jsonResponse = chatResponse.choices.firstOrNull()?.message?.content
            ?.trim()
            ?.removePrefix("```json")
            ?.removePrefix("```")
            ?.removeSuffix("```")
            ?.trim() ?: throw Exception("Empty response from AI")

        return Gson().fromJson(jsonResponse, GeneratedRecipe::class.java)
    }

    private fun generateRecipeId(recipe: GeneratedRecipe): String {
        val input = "${recipe.name}${recipe.ingredients.joinToString("")}${recipe.steps.joinToString("")}"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}