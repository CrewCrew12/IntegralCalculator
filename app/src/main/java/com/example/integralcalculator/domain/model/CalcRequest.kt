package com.example.integralcalculator.domain.model

import com.example.integralcalculator.domain.CalcMode

data class CalcRequest(
    val expression: String,
    val variable: String,
    val mode: CalcMode,
    val lowerLimit: String = "0",
    val upperLimit: String = "1"
)