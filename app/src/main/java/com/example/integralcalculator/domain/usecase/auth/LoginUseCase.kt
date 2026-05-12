package com.example.integralcalculator.domain.usecase.auth

import android.util.Patterns
import com.example.integralcalculator.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Некорректный Email"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль слишком короткий"))
        }
        return repository.login(email, password)
    }
}