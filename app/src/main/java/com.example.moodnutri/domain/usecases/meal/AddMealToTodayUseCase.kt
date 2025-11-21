package com.example.moodnutri.domain.usecases.meal

import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase

// ============================================
// AddMealToTodayUseCase.kt
// ============================================
data class AddMealParams(
    val recipeId: String = "scanned_meal_${System.currentTimeMillis()}",
    val calories: Int,
    val protein: Int,
    val carbs: Int
)

class AddMealToTodayUseCase(
    private val nutritionRepository: NutritionRepository
) : UseCase<AddMealParams, Result<Unit>> {

    override suspend fun invoke(params: AddMealParams): Result<Unit> {
        return nutritionRepository.addMealToToday(
            recipeId = params.recipeId,
            calories = params.calories,
            protein = params.protein,
            carbs = params.carbs
        )
    }
}