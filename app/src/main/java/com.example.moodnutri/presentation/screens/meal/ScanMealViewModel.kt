// File: presentation/screens/meal/ScanMealViewModel.kt
package com.example.moodnutri.presentation.screens.meal

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.MealIngredient
import com.example.moodnutri.data.models.openai.GeneratedRecipe
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.meal.AddMealParams
import com.example.moodnutri.domain.usecases.meal.AddMealToTodayUseCase
import com.example.moodnutri.domain.usecases.meal.AnalyzeMealParams
import com.example.moodnutri.domain.usecases.meal.AnalyzeMealUseCase
import com.example.moodnutri.domain.usecases.meal.CalculateNutritionUseCase
import com.example.moodnutri.domain.usecases.meal.NutritionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanMealViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ScanMealState>(ScanMealState.Idle)
    val uiState = _uiState.asStateFlow()

    val selectedImage = mutableStateOf<Bitmap?>(null)

    private val _nutritionInfo = MutableStateFlow(NutritionInfo())
    val nutritionInfo = _nutritionInfo.asStateFlow()

    val isCalculatingNutrition = mutableStateOf(false)

    val detectedIngredients = mutableStateListOf<MealIngredient>()

    // Use Cases
    private val analyzeMealUseCase = AnalyzeMealUseCase(
        preferencesManager = UserPreferencesManager(application)
    )

    private val calculateNutritionUseCase = CalculateNutritionUseCase()

    private val addMealToTodayUseCase = AddMealToTodayUseCase(
        nutritionRepository = NutritionRepository()
    )

    fun analyzeMeal(bitmap: Bitmap, baseRecipe: GeneratedRecipe? = null) {
        selectedImage.value = bitmap
        _uiState.value = ScanMealState.Loading
        detectedIngredients.clear()
        _nutritionInfo.value = NutritionInfo()

        viewModelScope.launch(Dispatchers.IO) {
            val params = AnalyzeMealParams(bitmap, baseRecipe)
            val result = analyzeMealUseCase(params)

            result.onSuccess { ingredients ->
                detectedIngredients.addAll(ingredients)
                _uiState.value = ScanMealState.Success
            }.onFailure { exception ->
                exception.printStackTrace()
                _uiState.value = ScanMealState.Error(
                    exception.message ?: "An unknown error occurred"
                )
            }
        }
    }

    fun calculateNutrition() {
        if (detectedIngredients.isEmpty()) return

        isCalculatingNutrition.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = calculateNutritionUseCase(detectedIngredients.toList())

            result.onSuccess { nutrition ->
                _nutritionInfo.value = nutrition
            }.onFailure { exception ->
                exception.printStackTrace()
                _nutritionInfo.value = NutritionInfo()
            }

            isCalculatingNutrition.value = false
        }
    }

    fun addMealToToday() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nutrition = _nutritionInfo.value
                if (nutrition.calories > 0) {
                    val params = AddMealParams(
                        calories = nutrition.calories,
                        protein = nutrition.protein,
                        carbs = nutrition.carbs
                    )

                    val result = addMealToTodayUseCase(params)

                    result.onSuccess {
                        android.util.Log.d("ScanMealVM", "Meal added to today successfully")
                    }.onFailure { exception ->
                        android.util.Log.e("ScanMealVM", "Error adding meal", exception)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ScanMealVM", "Error adding meal to today", e)
            }
        }
    }

    fun addIngredient(name: String, quantity: String) {
        if (name.isNotBlank()) {
            detectedIngredients.add(MealIngredient(name, quantity))
        }
    }

    fun removeIngredient(ingredient: MealIngredient) {
        detectedIngredients.remove(ingredient)
    }

    fun updateIngredient(index: Int, newName: String, newQuantity: String) {
        if (index in detectedIngredients.indices) {
            val originalId = detectedIngredients[index].id
            detectedIngredients[index] = MealIngredient(newName, newQuantity, id = originalId)
        }
    }

    fun clearScan() {
        selectedImage.value = null
        detectedIngredients.clear()
        _nutritionInfo.value = NutritionInfo()
        _uiState.value = ScanMealState.Idle
    }
}