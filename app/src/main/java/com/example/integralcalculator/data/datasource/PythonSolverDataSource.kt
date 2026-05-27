package com.example.integralcalculator.data.datasource

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PythonSolverDataSource @Inject constructor(
    private val context: Context
) {
    private val pythonMutex = Mutex()

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    suspend fun callIndefinite(expr: String, variable: String): String = pythonMutex.withLock {
        try {
            val py = Python.getInstance()
            val solver = py.getModule("solver")
            solver.callAttr("calculate_indefinite", expr, variable).toString()
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    suspend fun callDefinite(expr: String, variable: String, lower: String, upper: String): String = pythonMutex.withLock {
        try {
            val py = Python.getInstance()
            val solver = py.getModule("solver")
            solver.callAttr("calculate_definite", expr, variable, lower, upper).toString()
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }
}