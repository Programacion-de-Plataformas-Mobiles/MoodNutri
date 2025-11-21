package com.example.moodnutri.domain.usecases.auth

import com.example.moodnutri.domain.usecases.UseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

// ============================================
// SignUpUseCase.kt
// ============================================
data class SignUpParams(val email: String, val password: String)

class SignUpUseCase(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : UseCase<SignUpParams, Result<FirebaseUser>> {

    override suspend fun invoke(params: SignUpParams): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(params.email, params.password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up failed: user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}