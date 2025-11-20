package com.example.moodnutri.mockups

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.preferences.UserPreferencesManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    var mood by mutableStateOf("")
    var cookingTime by mutableStateOf("")

    val userIngredients = mutableStateListOf<String>()

    private val preferencesManager = UserPreferencesManager(application)

    val isFormValid: Boolean
        get() = mood.isNotBlank() && cookingTime.isNotBlank()

    fun addIngredient(ingredient: String) {
        if (ingredient.isNotBlank() && !userIngredients.contains(ingredient)) {
            userIngredients.add(ingredient)
        }
    }

    fun removeIngredient(ingredient: String) {
        userIngredients.remove(ingredient)
    }

    fun setIngredients(ingredients: List<String>){
        userIngredients.clear()
        userIngredients.addAll(ingredients.distinct())
    }

    fun saveMood() {
        if (mood.isNotBlank()) {
            val emoji = getMoodEmoji(mood)
            viewModelScope.launch {
                preferencesManager.setCurrentMood(mood, emoji)
            }
        }
    }

    private fun getMoodEmoji(mood: String): String {
        return when {
            mood.contains("happy", ignoreCase = true) || mood.contains("feliz", ignoreCase = true) -> "😊"
            mood.contains("sad", ignoreCase = true) || mood.contains("triste", ignoreCase = true) -> "😢"
            mood.contains("angry", ignoreCase = true) || mood.contains("enojado", ignoreCase = true) -> "😠"
            mood.contains("excited", ignoreCase = true) || mood.contains("emocionado", ignoreCase = true) -> "🤩"
            mood.contains("tired", ignoreCase = true) || mood.contains("cansado", ignoreCase = true) -> "😴"
            mood.contains("stressed", ignoreCase = true) || mood.contains("estresado", ignoreCase = true) -> "😰"
            mood.contains("calm", ignoreCase = true) || mood.contains("tranquilo", ignoreCase = true) -> "😌"
            mood.contains("love", ignoreCase = true) || mood.contains("amor", ignoreCase = true) -> "🥰"
            else -> "😊"
        }
    }
}