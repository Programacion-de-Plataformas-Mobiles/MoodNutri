package com.example.moodnutri.domain.usecases.profile

import android.net.Uri
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// SaveMoodUseCase.kt
// ============================================
data class SaveMoodParams(
    val mood: String,
    val emoji: String
)

class SaveMoodUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<SaveMoodParams, Unit> {

    override suspend fun invoke(params: SaveMoodParams) {
        preferencesManager.setCurrentMood(params.mood, params.emoji)
    }
}