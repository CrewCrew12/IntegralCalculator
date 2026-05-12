package com.example.integralcalculator.domain.usecase.auth

import com.example.integralcalculator.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Заполните все поля корректно"))
        }
        return repository.register(email, password)
    }
}