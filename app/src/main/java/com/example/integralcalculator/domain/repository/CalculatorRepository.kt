package com.example.integralcalculator.domain.repository

import com.example.integralcalculator.domain.model.CalcRequest
import com.example.integralcalculator.domain.model.CalcResult

interface CalculatorRepository {
    suspend fun calculate(request: CalcRequest): CalcResult
}