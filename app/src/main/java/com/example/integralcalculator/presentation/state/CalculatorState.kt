package com.example.integralcalculator.presentation.state

import com.example.integralcalculator.domain.model.IntegralResult

data class CalculatorState(
    val rawInput: String = "",
    val latexPreview: String = "",
    val cursorPosition: Int = 0,
    val result: IntegralResult? = null,
    val isLoading: Boolean = false,
    val isDefinite: Boolean = false,
    val integrationVar: String = "x",
    val lowerLimit: String = "a",
    val upperLimit: String = "b"
)