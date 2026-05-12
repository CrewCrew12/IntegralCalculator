package com.example.integralcalculator.data.datasource

import com.chaquo.python.Python

class PythonSolverDataSource {
    fun callIndefinite(expr: String, variable: String): Any =
        Python.getInstance().getModule("solver")
            .callAttr("calculate_indefinite", expr, variable)

    fun callDefinite(expr: String, variable: String, lower: String, upper: String): Any =
        Python.getInstance().getModule("solver")
            .callAttr("calculate_definite", expr, variable, lower, upper)
}