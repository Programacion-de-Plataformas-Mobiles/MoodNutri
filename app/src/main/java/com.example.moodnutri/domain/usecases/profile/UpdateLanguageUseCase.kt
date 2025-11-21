package com.example.moodnutri.domain.usecases.profile

import android.net.Uri
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// UpdateLanguageUseCase.kt
// ============================================
class UpdateLanguageUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<String, Unit> {

    override suspend fun invoke(params: String) {
        preferencesManager.setLanguage(params)
    }
}