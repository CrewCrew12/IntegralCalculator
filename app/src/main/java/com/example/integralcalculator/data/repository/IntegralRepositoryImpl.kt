package com.example.integralcalculator.data.repository

import com.chaquo.python.PyObject
import com.example.integralcalculator.data.datasource.PythonSolverDataSource
import com.example.integralcalculator.domain.model.IntegralResult
import com.example.integralcalculator.domain.repository.IntegralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntegralRepositoryImpl(
    private val dataSource: PythonSolverDataSource
) : IntegralRepository {

    override suspend fun calculateIndefinite(expression: String, variable: String): IntegralResult =
        withContext(Dispatchers.IO) {
            runCatching { mapResult(dataSource.callIndefinite(expression, variable)) }
                .getOrElse { IntegralResult(false, "", it.message) }
        }

    override suspend fun calculateDefinite(
        expression: String, variable: String, lower: String, upper: String
    ): IntegralResult = withContext(Dispatchers.IO) {
        runCatching { mapResult(dataSource.callDefinite(expression, variable, lower, upper)) }
            .getOrElse { IntegralResult(false, "", it.message) }
    }

    private fun mapResult(pyObj: Any): IntegralResult {
        val py = pyObj as PyObject
        return try {
            val success = py.get("success").toString().toBoolean()
            if (success) {
                val latex = py.get("latex").toString()
                IntegralResult(success = true, latex = latex)
            } else {
                val error = py.get("error").toString()
                IntegralResult(success = false, latex = "", error = error)
            }
        } catch (e: Exception) {
            IntegralResult(success = false, latex = "", error = "Ошибка парсинга: ${e.message}")
        }
    }
}