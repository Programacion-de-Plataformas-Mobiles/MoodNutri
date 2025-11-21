package com.example.moodnutri.domain.usecases.auth

import com.example.moodnutri.domain.usecases.UseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

// ============================================
// LogoutUseCase.kt
// ============================================
class LogoutUseCase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    operator fun invoke() {
        auth.signOut()
    }
}