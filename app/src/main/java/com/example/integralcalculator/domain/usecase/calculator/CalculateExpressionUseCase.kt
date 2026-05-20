package com.example.integralcalculator.domain.usecase.calculator

import com.example.integralcalculator.domain.CalcMode
import com.example.integralcalculator.domain.model.CalcRequest
import com.example.integralcalculator.domain.model.CalcResult
import com.example.integralcalculator.domain.repository.CalculatorRepository
import javax.inject.Inject

class CalculateExpressionUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {
    suspend operator fun invoke(request: CalcRequest): CalcResult {
        val result = repository.calculate(request)
        if (!result.success) return result
        val finalLatex = if (request.mode == CalcMode.INTEGRAL) {
            "${result.latex}, C \\in \\mathbb{R}"
        } else {
            result.latex
        }
        return CalcResult(success = true, latex = finalLatex, error = "")
    }
}