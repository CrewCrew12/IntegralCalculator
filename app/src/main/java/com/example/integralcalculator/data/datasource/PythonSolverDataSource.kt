package com.example.integralcalculator.data.datasource

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PythonSolverDataSource @Inject constructor(
    private val context: Context
) {
    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    fun callIndefinite(expr: String, variable: String): String {
        val py = Python.getInstance()
        val solver = py.getModule("solver")
        return solver.callAttr("calculate_indefinite", expr, variable).toString()
    }

    fun callDefinite(expr: String, variable: String, lower: String, upper: String): String {
        val py = Python.getInstance()
        val solver = py.getModule("solver")
        return solver.callAttr("calculate_definite", expr, variable, lower, upper).toString()
    }
}