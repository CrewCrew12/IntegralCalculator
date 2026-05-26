package com.example.integralcalculator.domain.model

data class IntegralResult(
    val success: Boolean,
    val latex: String,
    val plainText: String = "",
    val error: String? = null
)