package com.example.integralcalculator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.integralcalculator.presentation.viewmodel.AuthViewModel
import com.example.integralcalculator.ui.theme.IntegralCalculatorTheme

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntegralCalculatorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AuthScreenContent()
                }
            }
        }
    }

    @Composable
    fun AuthScreenContent() {
        val authViewModel: AuthViewModel = viewModel()
        val uiState by authViewModel.uiState.collectAsState()

        // Принудительная проверка при старте
        LaunchedEffect(Unit) {
            authViewModel.refreshAuthStatus()
        }

        // Реагируем на инициализацию
        LaunchedEffect(uiState.isInitialized, uiState.isLoggedIn) {
            if (uiState.isInitialized) {
                if (uiState.isLoggedIn) {
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        // Показываем нужный экран
        when {
            !uiState.isInitialized -> {
                LoadingScreen(onLoadingComplete = {})
            }
            else -> {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {}
                )
            }
        }
    }
}