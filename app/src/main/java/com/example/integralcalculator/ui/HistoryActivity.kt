package com.example.integralcalculator.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.integralcalculator.ui.theme.IntegralCalculatorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntegralCalculatorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HistoryScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}