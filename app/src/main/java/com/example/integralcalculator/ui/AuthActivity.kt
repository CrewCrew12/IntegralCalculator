package com.example.integralcalculator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.integralcalculator.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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

        var showLoading by remember { mutableStateOf(true) }
        var isAuthenticated by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(500)
            authViewModel.refreshAuthStatus()
        }

        LaunchedEffect(uiState.isLoggedIn, uiState.isChecking) {
            if (!uiState.isChecking) {
                if (uiState.isLoggedIn) {
                    showLoading = true
                    delay(1500)
                    isAuthenticated = true
                    startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                    finish()
                } else {
                    showLoading = false
                    isAuthenticated = false
                }
            }
        }

        when {
            uiState.isChecking -> {
                LoadingScreen(onLoadingComplete = {})
            }
            showLoading && uiState.isLoggedIn -> {
                LoadingScreen(onLoadingComplete = {})
            }
            !uiState.isLoggedIn -> {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                    }
                )
            }
        }
    }
}