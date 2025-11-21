package com.example.moodnutri.presentation.screens.ingredients

// ============================================
// ScanIngredientsState.kt (presentation/screens/ingredients/)
// ============================================
sealed interface ScanIngredientsState {
    object Idle : ScanIngredientsState
    object Loading : ScanIngredientsState
    object Success : ScanIngredientsState
    data class Error(val message: String) : ScanIngredientsState
}