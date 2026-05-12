package com.example.integralcalculator.domain.repository

import com.example.integralcalculator.domain.model.IntegralResult

interface IntegralRepository {
    suspend fun calculateIndefinite(expression: String, variable: String): IntegralResult
    suspend fun calculateDefinite(
        expression: String, variable: String, lower: String, upper: String
    ): IntegralResult
}