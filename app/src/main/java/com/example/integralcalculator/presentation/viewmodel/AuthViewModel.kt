package com.example.integralcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integralcalculator.domain.usecase.auth.GetCurrentUserUseCase
import com.example.integralcalculator.domain.usecase.auth.LoginUseCase
import com.example.integralcalculator.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val userId = getCurrentUserUseCase()
        _uiState.update {
            it.copy(
                isLoggedIn = userId != null,
                userId = userId
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(email, password)
            when {
                result.isSuccess -> {
                    val userId = result.getOrNull()
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, userId = userId)
                    }
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    _uiState.update {
                        it.copy(isLoading = false, error = error)
                    }
                }
            }
        }
    }
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(email, password)
            when {
                result.isSuccess -> {
                    val userId = result.getOrNull()
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, userId = userId)
                    }
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    _uiState.update {
                        it.copy(isLoading = false, error = error)
                    }
                }
            }
        }
    }
}