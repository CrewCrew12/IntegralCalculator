package com.example.integralcalculator.domain.usecase

import com.example.integralcalculator.domain.model.IntegralResult
import com.example.integralcalculator.domain.repository.IntegralRepository

class CalculateIntegralUseCase(
    private val repository: IntegralRepository
) {
    suspend operator fun invoke(
        expression: String,
        variable: String,
        isDefinite: Boolean,
        lowerLimit: String,
        upperLimit: String
    ): IntegralResult = if (isDefinite) {
        repository.calculateDefinite(expression, variable, lowerLimit, upperLimit)
    } else {
        repository.calculateIndefinite(expression, variable)
    }
}