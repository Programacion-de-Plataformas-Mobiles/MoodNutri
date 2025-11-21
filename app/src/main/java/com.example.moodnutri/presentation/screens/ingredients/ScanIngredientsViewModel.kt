// File: presentation/screens/ingredients/ScanIngredientsViewModel.kt
package com.example.moodnutri.presentation.screens.ingredients

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.domain.usecases.ingredients.AnalyzeIngredientsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanIngredientsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ScanIngredientsState>(ScanIngredientsState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val analyzeIngredientsUseCase = AnalyzeIngredientsUseCase(
        preferencesManager = UserPreferencesManager(application)
    )

    fun analyzeImage(bitmap: Bitmap, onIngredientsDetected: (List<String>) -> Unit) {
        selectedImage.value = bitmap
        _uiState.value = ScanIngredientsState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = analyzeIngredientsUseCase(bitmap)

            result.onSuccess { ingredients ->
                onIngredientsDetected(ingredients)
                _uiState.value = ScanIngredientsState.Success
            }.onFailure { exception ->
                exception.printStackTrace()
                _uiState.value = ScanIngredientsState.Error(
                    exception.message ?: "An unknown error occurred"
                )
            }
        }
    }

    fun clearScan() {
        selectedImage.value = null
        _uiState.value = ScanIngredientsState.Idle
    }
}