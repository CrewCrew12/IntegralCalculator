package com.example.integralcalculator.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.*

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var showLoading by remember { mutableStateOf(false) }

                    if (!showLoading) {
                        AuthScreen(
                            onAuthSuccess = {
                                showLoading = true
                            }
                        )
                    } else {
                        LoadingScreen(
                            onLoadingComplete = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}