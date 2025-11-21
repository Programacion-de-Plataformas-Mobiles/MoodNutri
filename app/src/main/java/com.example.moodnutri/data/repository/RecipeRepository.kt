// File: data/repository/RecipeRepository.kt
package com.example.moodnutri.data.repository

import com.example.moodnutri.data.models.themealdb.MealDetails
import com.example.moodnutri.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class RecipeRepository {

    private val mealDbService = RetrofitInstance.mealDbApi

    suspend fun findRecipesByIngredients(ingredients: List<String>): List<MealDetails> = withContext(Dispatchers.IO) {
        if (ingredients.isEmpty()) return@withContext emptyList()

        val mealSummaries = ingredients.map {
            async { mealDbService.searchByIngredient(it).meals ?: emptyList() }
        }.awaitAll().flatten()

        val uniqueMealIds = mealSummaries.map { it.idMeal }.distinct()

        val mealDetailsList = uniqueMealIds.map {
            async { mealDbService.getMealDetails(it).meals?.firstOrNull() }
        }.awaitAll().filterNotNull()

        return@withContext mealDetailsList
    }
}