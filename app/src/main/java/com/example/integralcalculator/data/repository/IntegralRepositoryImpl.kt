package com.example.integralcalculator.data.repository

import com.example.integralcalculator.data.datasource.PythonSolverDataSource
import com.example.integralcalculator.domain.model.IntegralResult
import com.example.integralcalculator.domain.repository.IntegralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegralRepositoryImpl @Inject constructor(
    private val dataSource: PythonSolverDataSource
) : IntegralRepository {

    override suspend fun calculateIndefinite(expression: String, variable: String): IntegralResult =
        withContext(Dispatchers.IO) {
            try {
                val result = dataSource.callIndefinite(expression, variable)
                parseResult(result)
            } catch (e: Exception) {
                IntegralResult(false, "", e.message)
            }
        }

    override suspend fun calculateDefinite(
        expression: String, variable: String, lower: String, upper: String
    ): IntegralResult = withContext(Dispatchers.IO) {
        try {
            val result = dataSource.callDefinite(expression, variable, lower, upper)
            parseResult(result)
        } catch (e: Exception) {
            IntegralResult(false, "", e.message)
        }
    }

    private fun parseResult(result: String): IntegralResult {
        return if (result.startsWith("ERROR:")) {
            val error = result.substring(6)
            IntegralResult(false, "", error)
        } else {
            IntegralResult(true, result)
        }
    }
}