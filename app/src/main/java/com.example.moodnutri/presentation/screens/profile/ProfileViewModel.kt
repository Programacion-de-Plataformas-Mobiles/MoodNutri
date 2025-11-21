// File: presentation/screens/profile/ProfileViewModel.kt
package com.example.moodnutri.presentation.screens.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.DailyNutrition
import com.example.moodnutri.data.preferences.UserPreferencesManager
import com.example.moodnutri.data.repository.NutritionRepository
import com.example.moodnutri.domain.usecases.profile.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = UserPreferencesManager(application)
    private val nutritionRepository = NutritionRepository()
    private val auth = FirebaseAuth.getInstance()

    // Use Cases
    private val loadTodayNutritionUseCase = LoadTodayNutritionUseCase(nutritionRepository)
    private val updateGoalsUseCase = UpdateGoalsUseCase(preferencesManager)
    private val updateLanguageUseCase = UpdateLanguageUseCase(preferencesManager)
    private val updateThemeUseCase = UpdateThemeUseCase(preferencesManager)
    private val updateProfilePhotoUseCase = UpdateProfilePhotoUseCase(preferencesManager)

    // Estados de UI
    private val _dailyCalorieGoal = MutableStateFlow(2000)
    val dailyCalorieGoal = _dailyCalorieGoal.asStateFlow()

    private val _dailyProteinGoal = MutableStateFlow(150)
    val dailyProteinGoal = _dailyProteinGoal.asStateFlow()

    private val _dailyCarbsGoal = MutableStateFlow(250)
    val dailyCarbsGoal = _dailyCarbsGoal.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

    private val _themeMode = MutableStateFlow("system")
    val themeMode = _themeMode.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow<String?>(null)
    val profilePhotoUri = _profilePhotoUri.asStateFlow()

    private val _todayNutrition = MutableStateFlow(DailyNutrition())
    val todayNutrition = _todayNutrition.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentMood = MutableStateFlow("Happy")
    val currentMood = _currentMood.asStateFlow()

    private val _currentEmoji = MutableStateFlow("😊")
    val currentEmoji = _currentEmoji.asStateFlow()

    init {
        loadPreferences()
        loadTodayNutrition()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.dailyCalorieGoal.collect { _dailyCalorieGoal.value = it }
        }
        viewModelScope.launch {
            preferencesManager.dailyProteinGoal.collect { _dailyProteinGoal.value = it }
        }
        viewModelScope.launch {
            preferencesManager.dailyCarbsGoal.collect { _dailyCarbsGoal.value = it }
        }
        viewModelScope.launch {
            preferencesManager.language.collect { _language.value = it }
        }
        viewModelScope.launch {
            preferencesManager.themeMode.collect { _themeMode.value = it }
        }
        viewModelScope.launch {
            preferencesManager.profilePhotoUri.collect { _profilePhotoUri.value = it }
        }
        viewModelScope.launch {
            preferencesManager.currentMood.collect { _currentMood.value = it }
        }
        viewModelScope.launch {
            preferencesManager.currentEmoji.collect { _currentEmoji.value = it }
        }
    }

    fun loadTodayNutrition() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = loadTodayNutritionUseCase()

            result.onSuccess { nutrition ->
                _todayNutrition.value = nutrition
                Log.d("ProfileVM", "Today's nutrition: ${nutrition.caloriesConsumed} cal")
            }.onFailure { error ->
                Log.e("ProfileVM", "Error loading nutrition", error)
            }

            _isLoading.value = false
        }
    }

    fun setDailyCalorieGoal(calories: Int) {
        viewModelScope.launch {
            val params = UpdateGoalsParams(calories = calories)
            updateGoalsUseCase(params)
            _dailyCalorieGoal.value = calories
        }
    }

    fun setDailyProteinGoal(protein: Int) {
        viewModelScope.launch {
            val params = UpdateGoalsParams(protein = protein)
            updateGoalsUseCase(params)
            _dailyProteinGoal.value = protein
        }
    }

    fun setDailyCarbsGoal(carbs: Int) {
        viewModelScope.launch {
            val params = UpdateGoalsParams(carbs = carbs)
            updateGoalsUseCase(params)
            _dailyCarbsGoal.value = carbs
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            updateLanguageUseCase(lang)
            _language.value = lang
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            updateThemeUseCase(mode)
            _themeMode.value = mode
        }
    }

    fun setProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            updateProfilePhotoUseCase(uri)
            _profilePhotoUri.value = uri.toString()
        }
    }

    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: ""
    }
}