package com.example.integralcalculator.domain.usecase.auth

import com.example.integralcalculator.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() {
        repository.logout()
    }
}