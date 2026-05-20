package com.example.integralcalculator.domain.model

data class CalcResult(
    val success: Boolean,
    val latex: String = "",
    val error: String = ""
)