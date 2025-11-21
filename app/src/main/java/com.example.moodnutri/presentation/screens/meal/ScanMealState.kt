package com.example.moodnutri.presentation.screens.meal

// ============================================
// ScanMealState.kt (presentation/screens/meal/)
// ============================================
sealed interface ScanMealState {
    object Idle : ScanMealState
    object Loading : ScanMealState
    object Success : ScanMealState
    data class Error(val message: String) : ScanMealState
}