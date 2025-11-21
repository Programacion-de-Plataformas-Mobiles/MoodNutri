package com.example.moodnutri.domain.usecases.profile

import android.net.Uri
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// UpdateGoalsUseCase.kt
// ============================================
data class UpdateGoalsParams(
    val calories: Int? = null,
    val protein: Int? = null,
    val carbs: Int? = null
)

class UpdateGoalsUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<UpdateGoalsParams, Unit> {

    override suspend fun invoke(params: UpdateGoalsParams) {
        params.calories?.let { preferencesManager.setDailyCalorieGoal(it) }
        params.protein?.let { preferencesManager.setDailyProteinGoal(it) }
        params.carbs?.let { preferencesManager.setDailyCarbsGoal(it) }
    }
}