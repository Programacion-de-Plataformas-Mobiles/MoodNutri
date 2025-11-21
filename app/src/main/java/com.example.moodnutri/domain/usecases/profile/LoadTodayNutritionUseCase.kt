package com.example.moodnutri.domain.usecases.profile

import android.net.Uri
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// LoadTodayNutritionUseCase.kt
// ============================================
class LoadTodayNutritionUseCase(
    private val nutritionRepository: NutritionRepository
) : UseCaseNoParams<Result<DailyNutrition>> {

    override suspend fun invoke(): Result<DailyNutrition> {
        return nutritionRepository.getTodayNutrition()
    }
}