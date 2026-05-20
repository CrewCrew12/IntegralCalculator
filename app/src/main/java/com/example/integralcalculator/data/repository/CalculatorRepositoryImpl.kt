package com.example.integralcalculator.data.repository

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.integralcalculator.domain.CalcMode
import com.example.integralcalculator.domain.model.CalcRequest
import com.example.integralcalculator.domain.model.CalcResult
import com.example.integralcalculator.domain.repository.CalculatorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculatorRepositoryImpl @Inject constructor(
    private val context: Context
) : CalculatorRepository {

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }
    override suspend fun calculate(request: CalcRequest): CalcResult = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val solver = py.getModule("solver")

            val pyResult = when (request.mode) {
                CalcMode.DEFINITE -> solver.callAttr(
                    "calculate_definite",
                    request.expression,
                    request.variable,
                    request.lowerLimit,
                    request.upperLimit
                )
                CalcMode.DERIVATIVE -> solver.callAttr(
                    "calculate_derivative",
                    request.expression,
                    request.variable
                )
                else -> solver.callAttr(
                    "calculate_indefinite",
                    request.expression,
                    request.variable
                )
            }
            val success = pyResult.get("success")?.toBoolean() ?: false
            val latex = pyResult.get("latex")?.toString() ?: ""
            val error = pyResult.get("error")?.toString() ?: ""

            CalcResult(success, latex, error)
        } catch (e: Exception) {
            CalcResult(false, "", e.message ?: "Unknown error")
        }
    }
}