// File: presentation/screens/auth/AuthViewModel.kt
package com.example.moodnutri.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodnutri.domain.usecases.auth.LoginParams
import com.example.moodnutri.domain.usecases.auth.LoginUseCase
import com.example.moodnutri.domain.usecases.auth.LogoutUseCase
import com.example.moodnutri.domain.usecases.auth.SignUpParams
import com.example.moodnutri.domain.usecases.auth.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val loginUseCase = LoginUseCase()
    private val signUpUseCase = SignUpUseCase()
    private val logoutUseCase = LogoutUseCase()

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        _currentUser.value = auth.currentUser
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val params = SignUpParams(email, password)
            val result = signUpUseCase(params)

            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val params = LoginParams(email, password)
            val result = loginUseCase(params)

            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error occurred")
            }
        }
    }

    fun logout() {
        logoutUseCase()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}