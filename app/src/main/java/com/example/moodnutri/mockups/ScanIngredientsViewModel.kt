package com.example.moodnutri.mockups

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.BuildConfig
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    object Success : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class ScanIngredientsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val preferencesManager = UserPreferencesManager(application)

    fun analyzeImage(bitmap: Bitmap, onIngredientsDetected: (List<String>) -> Unit) {
        selectedImage.value = bitmap
        _uiState.value = ScanUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val language = preferencesManager.language.firstOrNull() ?: "en"
                val languageName = when(language) {
                    "es" -> "Spanish"
                    "fr" -> "French"
                    else -> "English"
                }

                val prompt = """
                    Analyze this image and identify all visible food ingredients.
                    
                    IMPORTANT: Respond in $languageName language.
                    
                    Return ONLY a JSON array of ingredient names in $languageName.
                    Format: ["ingredient 1 in $languageName", "ingredient 2 in $languageName", ...]
                    
                    Example for $languageName:
                    ["Ingredient name 1", "Ingredient name 2", "Ingredient name 3"]
                    
                    Do not include quantities, just ingredient names.
                    Do not include any other text or markdown formatting.
                """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)

                val jsonString = response.text
                    ?.trim()
                    ?.removePrefix("```json")
                    ?.removeSuffix("```")
                    ?: "[]"

                val itemType = object : TypeToken<List<String>>() {}.type
                val ingredients: List<String> = Gson().fromJson(jsonString, itemType)

                onIngredientsDetected(ingredients)
                _uiState.value = ScanUiState.Success

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ScanUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun clearScan() {
        selectedImage.value = null
        _uiState.value = ScanUiState.Idle
    }
}