package com.example.moodnutri.mockups

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val ingredients: List<String>) : ScanUiState
    data class Error(val message: String) : ScanUiState
}