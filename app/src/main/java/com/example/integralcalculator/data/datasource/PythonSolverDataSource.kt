package com.example.integralcalculator.data.datasource

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PythonSolverDataSource @Inject constructor(
    private val context: Context
) {
    private val pythonMutex = Mutex()
    private val indefiniteCache = ConcurrentHashMap<String, String>()
    private val definiteCache = ConcurrentHashMap<String, String>()

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    suspend fun callIndefinite(expr: String, variable: String): String = pythonMutex.withLock {
        val cacheKey = "$expr|$variable"
        indefiniteCache[cacheKey]?.let { return it }

        try {
            val py = Python.getInstance()
            val solver = py.getModule("solver")
            val result = solver.callAttr("calculate_indefinite", expr, variable).toString()
            if (result.isNotBlank() && !result.startsWith("Ошибка")) {
                indefiniteCache[cacheKey] = result
            }

            return result
        } catch (e: Exception) {
            return "Ошибка: ${e.message}"
        }
    }

    suspend fun callDefinite(expr: String, variable: String, lower: String, upper: String): String = pythonMutex.withLock {
        val cacheKey = "$expr|$variable|$lower|$upper"
        definiteCache[cacheKey]?.let { return it }

        try {
            val py = Python.getInstance()
            val solver = py.getModule("solver")
            val result = solver.callAttr("calculate_definite", expr, variable, lower, upper).toString()
            if (result.isNotBlank() && !result.startsWith("Ошибка")) {
                definiteCache[cacheKey] = result
            }

            return result
        } catch (e: Exception) {
            return "Ошибка: ${e.message}"
        }
    }

    fun clearCache() {
        indefiniteCache.clear()
        definiteCache.clear()
    }
}