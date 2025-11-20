package com.example.moodnutri.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.models.firebase.FirebaseRecipe
import com.example.moodnutri.data.repository.FirebaseRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeRecipesViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRecipeRepository()

    private val _savedRecipes = MutableStateFlow<List<FirebaseRecipe>>(emptyList())
    val savedRecipes = _savedRecipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadSavedRecipes() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = firebaseRepository.getSavedRecipes()
            result.onSuccess { recipes ->
                _savedRecipes.value = recipes
            }

            _isLoading.value = false
        }
    }
}