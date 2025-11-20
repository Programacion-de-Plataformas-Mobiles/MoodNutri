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

// Este ViewModel ahora solo maneja el estado del escaneo, no la lista de ingredientes.
class ScanIngredientsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun analyzeImage(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
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
                
                // Pasamos el resultado al HomeViewModel
                onResult(ingredients)
                _uiState.value = ScanUiState.Success(ingredients)

            } catch (e: Exception) {
                println("Gemini API Error: $e")
                _uiState.value = ScanUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun clearScan() {
        selectedImage.value = null
        _uiState.value = ScanUiState.Idle
    }
}
