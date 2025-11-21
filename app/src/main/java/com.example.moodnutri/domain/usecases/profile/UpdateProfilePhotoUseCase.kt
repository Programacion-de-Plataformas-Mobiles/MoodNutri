package com.example.moodnutri.domain.usecases.profile

import android.net.Uri
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.UseCase
import com.example.moodnutri.domain.usecases.UseCaseNoParams

// ============================================
// UpdateProfilePhotoUseCase.kt
// ============================================
class UpdateProfilePhotoUseCase(
    private val preferencesManager: UserPreferencesManager
) : UseCase<Uri, Unit> {

    override suspend fun invoke(params: Uri) {
        preferencesManager.setProfilePhotoUri(params.toString())
    }
}