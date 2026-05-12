package com.example.integralcalculator.data.datasource

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.PyObject
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

    fun callIndefinite(expr: String, variable: String): PyObject {
        val py = Python.getInstance()
        val solver = py.getModule("solver")
        return solver.callAttr("calculate_indefinite", expr, variable) as PyObject
    }

    fun callDefinite(expr: String, variable: String, lower: String, upper: String): PyObject {
        val py = Python.getInstance()
        val solver = py.getModule("solver")
        return solver.callAttr("calculate_definite", expr, variable, lower, upper) as PyObject
    }
}