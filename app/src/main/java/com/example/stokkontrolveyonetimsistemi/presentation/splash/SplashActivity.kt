package com.example.stokkontrolveyonetimsistemi.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.stokkontrolveyonetimsistemi.core.constants.AppConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.AutoLoginUseCase
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginActivity
import com.example.stokkontrolveyonetimsistemi.presentation.auth.splash.SplashScreenContent
import com.example.stokkontrolveyonetimsistemi.presentation.main.DashboardActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Splash Screen Activity
 * Auto-login kontrolü ve app initialization
 *
 * UPDATED NAVIGATION FLOW:
 * SplashActivity → LoginActivity (auth required)
 * SplashActivity → DashboardActivity (auto-login success)
 */
@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    // Dependency injection
    private val autoLoginUseCase: AutoLoginUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Modern back handling - exit app
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back pressed - exiting app")
                finishAffinity()
            }
        })

        Log.d(TAG, "Splash screen started")

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplashScreen()
                }
            }
        }

        // Auto-login kontrolünü başlat
        performAppInitialization()
    }

    /**
     * App initialization ve auto-login kontrolü
     */
    private fun performAppInitialization() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting app initialization...")

                // Minimum splash duration için delay
                delay(AppConstants.SPLASH_DISPLAY_DURATION_MS)

                // Auto-login kontrolü
                autoLoginUseCase.execute().collect { authState ->
                    handleInitializationResult(authState)
                }

            } catch (e: Exception) {
                Log.e(TAG, "App initialization failed", e)
                navigateToLogin("Başlatma hatası")
            }
        }
    }

    /**
     * Initialization sonucunu handle et ve navigation yap
     * ✅ UPDATED: DashboardActivity'ye yönlendirme
     */
    private fun handleInitializationResult(authState: AuthState) {
        when (authState) {
            is AuthState.Success -> {
                Log.d(TAG, "Auto-login successful - navigating to Dashboard")
                navigateToDashboard("Otomatik giriş başarılı")
            }

            is AuthState.TokenExpired -> {
                Log.d(TAG, "Token expired - navigating to login")
                navigateToLogin("Oturum süresi dolmuş, lütfen tekrar giriş yapın")
            }

            is AuthState.Error -> {
                Log.d(TAG, "Auto-login failed - navigating to login: ${authState.message}")
                navigateToLogin(null) // Normal login flow
            }

            is AuthState.Unauthorized -> {
                Log.d(TAG, "Unauthorized - navigating to login")
                navigateToLogin("Yetkilendirme gerekli")
            }

            is AuthState.Loading -> {
                // Still loading, wait for next state
                Log.d(TAG, "Still loading...")
            }

            AuthState.Idle -> {
                Log.d(TAG, "Idle state - navigating to login")
                navigateToLogin(null)
            }
        }
    }

    /**
     * Navigate to Dashboard Activity (authentication successful)
     * ✅ UPDATED: DashboardActivity'ye direkt yönlendirme
     */
    private fun navigateToDashboard(message: String?) {
        Log.d(TAG, "Navigating to DashboardActivity")

        val intent = Intent(this, DashboardActivity::class.java).apply {
            if (!message.isNullOrBlank()) {
                putExtra("auth_message", message)
            }
            // Clear task stack - splash screen'i geçmişte bırakma
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    /**
     * Navigate to login activity (authentication required)
     */
    private fun navigateToLogin(message: String?) {
        Log.d(TAG, "Navigating to LoginActivity")

        val intent = Intent(this, LoginActivity::class.java).apply {
            if (!message.isNullOrBlank()) {
                putExtra("auth_message", message)
            }
            // Clear task stack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    /**
     * Handle back button - exit app instead of going back
     * ✅ FIXED: Call super.onBackPressed() first
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Call super first, then exit app
        super.onBackPressed()
        finishAffinity()
    }
}

/**
 * Splash Screen Composable wrapper
 */
@Composable
private fun SplashScreen() {
    // Splash UI state
    val isLoading by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf("Uygulama başlatılıyor...") }

    // Status message updates
    LaunchedEffect(Unit) {
        delay(500)
        statusMessage = "Güvenlik kontrolü..."
        delay(1000)
        statusMessage = "Bağlantı kontrol ediliyor..."
        delay(500)
        statusMessage = "Hazırlanıyor..."
    }

    // Splash screen UI
    SplashScreenContent(
        isLoading = isLoading,
        statusMessage = statusMessage,
        appVersion = AppConstants.APP_VERSION,
        companyName = AppConstants.COMPANY_NAME
    )
}