package com.example.integralcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.usecase.CalculateIntegralUseCase
import com.example.integralcalculator.domain.usecase.auth.GetCurrentUserUseCase
import com.example.integralcalculator.domain.usecase.history.SaveHistoryRecordUseCase
import com.example.integralcalculator.presentation.state.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val calculateUseCase: CalculateIntegralUseCase,
    private val saveHistoryUseCase: SaveHistoryRecordUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private fun inputToLatex(raw: String): String {
        if (raw.isEmpty()) return ""
        android.util.Log.d("CalculatorVM", "Converting: $raw")
        var latex = raw
            .replace("()/()", "\\frac{}{}")
            .replace("sqrt(", "\\sqrt{")
            .replace("sin(", "\\sin\\left(")
            .replace("cos(", "\\cos\\left(")
            .replace("tan(", "\\tan\\left(")
            .replace("cot(", "\\cot\\left(")
            .replace("asin(", "\\arcsin\\left(")
            .replace("acos(", "\\arccos\\left(")
            .replace("atan(", "\\arctan\\left(")
            .replace("acot(", "\\arccot\\left(")
            .replace("log(", "\\log\\left(")
            .replace("ln(", "\\ln\\left(")
            .replace("exp(", "e^{")
            .replace("abs(", "\\left|")
            .replace("pi", "\\pi")
            .replace("α", "\\alpha")
            .replace("β", "\\beta")
            .replace("^", "^{")
            .replace("*", "\\cdot ")
        val leftCount = latex.count { it == '(' }
        val rightCount = latex.count { it == ')' }
        if (leftCount > rightCount) {
            latex += ")".repeat(leftCount - rightCount)
        }
        val openBraceCount = latex.count { it == '{' }
        val closeBraceCount = latex.count { it == '}' }
        if (openBraceCount > closeBraceCount) {
            latex += "}".repeat(openBraceCount - closeBraceCount)
        }
        android.util.Log.d("CalculatorVM", "Result: $latex")
        return latex
    }

    fun appendInput(text: String, latex: String) {
        _state.update {
            val newRaw = it.rawInput + text
            it.copy(rawInput = newRaw, latexPreview = inputToLatex(newRaw))
        }
    }

    fun backspace() {
        _state.update {
            if (it.rawInput.isEmpty()) return@update it
            val newRaw = it.rawInput.dropLast(1)
            it.copy(rawInput = newRaw, latexPreview = inputToLatex(newRaw))
        }
    }

    fun clear() {
        _state.update { it.copy(rawInput = "", latexPreview = "", result = null) }
    }

    fun setMode(isDefinite: Boolean) {
        _state.update { it.copy(isDefinite = isDefinite, result = null) }
    }

    fun setLimits(lower: String, upper: String) {
        _state.update { it.copy(lowerLimit = lower, upperLimit = upper) }
    }

    fun setVariable(variable: String) {
        _state.update { it.copy(integrationVar = variable) }
    }

    fun calculate() {
        viewModelScope.launch {
            val current = _state.value
            if (current.rawInput.isBlank()) return@launch

            _state.update { it.copy(isLoading = true, result = null) }

            val result = calculateUseCase(
                current.rawInput, current.integrationVar,
                current.isDefinite, current.lowerLimit, current.upperLimit
            )

            // 🔥 СОХРАНЯЕМ В ИСТОРИЮ, если пользователь авторизован
            if (result.success) {
                val userId = getCurrentUserUseCase()
                if (userId != null) {
                    val record = CalcRecord(
                        expression = current.rawInput,
                        variable = current.integrationVar,
                        result = result.latex,
                        timestamp = System.currentTimeMillis(),
                        isDefinite = current.isDefinite
                    )
                    saveHistoryUseCase(userId, record)
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    result = result,
                    latexPreview = if (!result.success) "\\text{Ошибка: } ${result.error}" else it.latexPreview
                )
            }
        }
    }
}