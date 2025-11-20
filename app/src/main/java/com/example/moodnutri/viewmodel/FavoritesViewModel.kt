package com.example.moodnutri.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.data.Recipe
import com.example.moodnutri.data.RecipeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val recipeDao: RecipeDao) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites = _favorites.asStateFlow()

    fun getFavorites() {
        viewModelScope.launch {
            _favorites.value = recipeDao.getFavoriteRecipes()
        }
    }
}