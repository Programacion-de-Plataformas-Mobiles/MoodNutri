package com.example.moodnutri.mockups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var mood by mutableStateOf("")
    var cookingTime by mutableStateOf("")

    val userIngredients = mutableStateListOf<String>()

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
}
