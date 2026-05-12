package com.example.integralcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integralcalculator.domain.usecase.CalculateIntegralUseCase
import com.example.integralcalculator.presentation.state.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel @Inject constructor(
    private val calculateUseCase: CalculateIntegralUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private fun inputToLatex(raw: String): String {
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
            .replace(" + ", " + ")
            .replace(" - ", " - ")
            .replace(" / ", " / ")
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