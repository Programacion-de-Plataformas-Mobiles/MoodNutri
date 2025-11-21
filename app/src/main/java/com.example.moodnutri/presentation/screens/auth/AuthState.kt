package com.example.moodnutri.presentation.screens.auth

import com.google.firebase.auth.FirebaseUser

// ============================================
// AuthState.kt (presentation/screens/auth/)
// ============================================
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val user: FirebaseUser) : AuthState
    data class Error(val message: String) : AuthState
}