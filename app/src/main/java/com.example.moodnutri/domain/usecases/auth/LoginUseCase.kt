package com.example.moodnutri.domain.usecases.auth

import com.example.moodnutri.domain.usecases.UseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

// ============================================
// LoginUseCase.kt
// ============================================
data class LoginParams(val email: String, val password: String)

class LoginUseCase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UseCase<LoginParams, Result<FirebaseUser>> {

    override suspend fun invoke(params: LoginParams): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(params.email, params.password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed: user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}