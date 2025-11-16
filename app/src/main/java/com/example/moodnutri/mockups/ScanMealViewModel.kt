package com.example.moodnutri.mockups

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanMealViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun analyzeImage(bitmap: Bitmap) {
        selectedImage.value = bitmap
        _uiState.value = ScanUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt =
                    "Identify all the ingredients visible in this image. Return only a comma-separated list."

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)

                val ingredients =
                    response.text?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: emptyList()

                _uiState.value = ScanUiState.Success(ingredients)

            } catch (e: Exception) {
                println("Gemini API Error in ScanMealViewModel: $e")
                _uiState.value = ScanUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun addIngredient(ingredient: String) {
        val current = _uiState.value
        if (ingredient.isNotBlank() && current is ScanUiState.Success) {
            _uiState.value = ScanUiState.Success(current.ingredients + ingredient)
        }
    }

    fun removeIngredient(ingredient: String) {
        val current = _uiState.value
        if (current is ScanUiState.Success) {
            _uiState.value =
                ScanUiState.Success(current.ingredients.filter { it != ingredient })
        }
    }
}
