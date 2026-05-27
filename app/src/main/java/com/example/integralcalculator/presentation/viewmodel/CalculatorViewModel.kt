package com.example.integralcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.usecase.CalculateIntegralUseCase
import com.example.integralcalculator.domain.usecase.auth.GetCurrentUserUseCase
import com.example.integralcalculator.domain.usecase.history.SaveHistoryRecordUseCase
import com.example.integralcalculator.presentation.state.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var lastCalculationJob: Job? = null

    private fun inputToLatex(raw: String): String {
        if (raw.isEmpty()) return ""
        android.util.Log.d("CalculatorVM", "Converting: $raw")

        var latex = raw
            .replace("()/()", "\\frac{}{}")
            .replace("sqrt(", "\\sqrt{")
            .replace("sin(", "\\sin(")
            .replace("cos(", "\\cos(")
            .replace("tan(", "\\tan(")
            .replace("cot(", "\\cot(")
            .replace("asin(", "\\arcsin(")
            .replace("acos(", "\\arccos(")
            .replace("atan(", "\\arctan(")
            .replace("acot(", "\\arccot(")
            .replace("log(", "\\log(")
            .replace("ln(", "\\ln(")
            .replace("exp(", "e^{")
            .replace("abs(", "|")
            .replace("pi", "\\pi")
            .replace("α", "\\alpha")
            .replace("β", "\\beta")
            .replace("^", "^{")
            .replace("*", "\\cdot ")

        var result = latex
        val leftCount = result.count { it == '(' }
        val rightCount = result.count { it == ')' }
        if (leftCount > rightCount) {
            result += ")".repeat(leftCount - rightCount)
        }

        val openBraceCount = result.count { it == '{' }
        val closeBraceCount = result.count { it == '}' }
        if (openBraceCount > closeBraceCount) {
            result += "}".repeat(openBraceCount - closeBraceCount)
        }

        android.util.Log.d("CalculatorVM", "Result: $result")
        return result
    }

    fun appendInput(text: String, latex: String) {
        _state.update { currentState ->
            val cursorPos = currentState.cursorPosition.coerceIn(0, currentState.rawInput.length)
            val newRaw = currentState.rawInput.substring(0, cursorPos) + text + currentState.rawInput.substring(cursorPos)
            currentState.copy(
                rawInput = newRaw,
                latexPreview = inputToLatex(newRaw),
                cursorPosition = cursorPos + text.length
            )
        }
    }

    fun moveCursorLeft() {
        _state.update { currentState ->
            val newPos = (currentState.cursorPosition - 1).coerceAtLeast(0)
            currentState.copy(cursorPosition = newPos)
        }
    }

    fun moveCursorRight() {
        _state.update { currentState ->
            val newPos = (currentState.cursorPosition + 1).coerceAtMost(currentState.rawInput.length)
            currentState.copy(cursorPosition = newPos)
        }
    }

    fun backspace() {
        _state.update { currentState ->
            if (currentState.rawInput.isEmpty()) return@update currentState
            val cursorPos = currentState.cursorPosition.coerceIn(0, currentState.rawInput.length)
            if (cursorPos == 0) return@update currentState
            val newRaw = currentState.rawInput.substring(0, cursorPos - 1) + currentState.rawInput.substring(cursorPos)
            currentState.copy(
                rawInput = newRaw,
                latexPreview = inputToLatex(newRaw),
                cursorPosition = cursorPos - 1
            )
        }
    }

    fun appendNumber(number: String) {
        val current = _state.value
        val lastChar = if (current.rawInput.isNotEmpty()) current.rawInput.last() else null
        val newRaw = if (lastChar != null && lastChar.isLetter() && lastChar != 'x') {
            current.rawInput + "*" + number
        } else {
            current.rawInput + number
        }
        _state.update {
            it.copy(rawInput = newRaw, latexPreview = inputToLatex(newRaw))
        }
    }

    fun clear() {
        _state.update {
            it.copy(
                rawInput = "",
                latexPreview = "",
                result = null,
                cursorPosition = 0
            )
        }
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
        lastCalculationJob?.cancel()

        lastCalculationJob = viewModelScope.launch {
            val current = _state.value
            if (current.rawInput.isBlank()) return@launch
            if (current.isLoading) return@launch

            _state.update { it.copy(isLoading = true, result = null) }

            val result = calculateUseCase(
                current.rawInput, current.integrationVar,
                current.isDefinite, current.lowerLimit, current.upperLimit
            )

            if (result.success) {
                val userId = getCurrentUserUseCase()
                if (userId != null) {
                    val record = CalcRecord(
                        expression = current.rawInput,
                        variable = current.integrationVar,
                        result = result.latex,
                        timestamp = System.currentTimeMillis(),
                        isDefinite = current.isDefinite,
                        lowerLimit = current.lowerLimit,
                        upperLimit = current.upperLimit
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