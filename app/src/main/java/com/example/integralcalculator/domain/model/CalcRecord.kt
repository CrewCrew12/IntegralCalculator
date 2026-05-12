package com.example.integralcalculator.domain.model

data class CalcRecord(
    val id: Long = 0,
    val expression: String,
    val variable: String,
    val result: String,
    val timestamp: Long,
    val isDefinite: Boolean
)