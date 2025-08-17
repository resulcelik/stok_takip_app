package com.example.stokkontrolveyonetimsistemi.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.presentation.main.DashboardActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Login Activity
 * Professional authentication screen with Material Design 3
 * JWT authentication + Remember Me + Password Reset
 *
 * ✅ UPDATED NAVIGATION: Login Success → DashboardActivity
 */
class LoginActivity : ComponentActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    // ViewModel dependency injection
    private val loginViewModel: LoginViewModel by viewModel()

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

        Log.d(TAG, "LoginActivity started")

        // Handle incoming auth message (from SplashActivity)
        val authMessage = intent.getStringExtra("auth_message")
        if (!authMessage.isNullOrBlank()) {
            Log.d(TAG, "Auth message received: $authMessage")
            showAuthMessage(authMessage)
        }

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = { message ->
                            handleLoginSuccess(message)
                        },
                        onPasswordResetRequest = { email ->
                            handlePasswordResetRequest(email)
                        }
                    )
                }
            }
        }

        // Observe authentication state
        observeAuthenticationState()
    }

    /**
     * Observe authentication state changes
     */
    private fun observeAuthenticationState() {
        lifecycleScope.launch {
            loginViewModel.authState.collect { authState ->
                handleAuthStateChange(authState)
            }
        }

        // Password reset state observation
        lifecycleScope.launch {
            loginViewModel.resetState.collect { resetState ->
                handleResetStateChange(resetState)
            }
        }
    }

    /**
     * Handle authentication state changes
     */
    private fun handleAuthStateChange(authState: AuthState) {
        when (val currentState = authState) {
            is AuthState.Success -> {
                Log.d(TAG, "Authentication successful")
                handleLoginSuccess(currentState.message)
            }

            is AuthState.Error -> {
                Log.w(TAG, "Authentication failed: ${currentState.message}")
                showToast(currentState.message)
            }

            is AuthState.TokenExpired -> {
                Log.w(TAG, "Token expired")
                showToast("Oturum süresi dolmuş, lütfen tekrar giriş yapın")
            }

            is AuthState.Loading -> {
                Log.d(TAG, "Authentication in progress")
            }

            AuthState.Idle -> {
                // Idle state
            }

            AuthState.Unauthorized -> {
                Log.w(TAG, "Unauthorized")
                showToast("Yetkilendirme hatası")
            }
        }
    }

    /**
     * Handle password reset state changes
     */
    private fun handleResetStateChange(resetState: AuthState) {
        when (val currentResetState = resetState) {
            is AuthState.Success -> {
                Log.d(TAG, "Password reset successful")
                showToast(currentResetState.message ?: "İşlem başarılı")
            }

            is AuthState.Error -> {
                Log.w(TAG, "Password reset failed: ${currentResetState.message}")
                showToast(currentResetState.message)
            }

            else -> {
                // Other states handled by UI
            }
        }
    }

    /**
     * Handle successful login
     * ✅ UPDATED: DashboardActivity'ye yönlendirme
     */
    private fun handleLoginSuccess(message: String?) {
        Log.d(TAG, "Login successful, navigating to DashboardActivity")

        // Show success message
        if (!message.isNullOrBlank()) {
            showToast(message)
        }

        // Navigate to DashboardActivity (not MainActivity)
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (!message.isNullOrBlank()) {
                putExtra("login_message", message)
            }
        }

        startActivity(intent)
        finish()
    }

    /**
     * Handle password reset request - Navigate to ForgotPasswordActivity
     */
    private fun handlePasswordResetRequest(email: String) {
        Log.d(TAG, "Password reset requested, navigating to ForgotPasswordActivity")
        val intent = Intent(this, com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot.ForgotPasswordActivity::class.java).apply {
            if (email.isNotEmpty()) {
                putExtra(com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot.ForgotPasswordActivity.EXTRA_EMAIL, email)
            }
        }
        startActivity(intent)
    }

    /**
     * Show incoming auth message
     */
    private fun showAuthMessage(message: String) {
        showToast(message)
    }

    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Handle back button - exit app
     * ✅ FIXED: Call super.onBackPressed() first
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Close entire app
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LoginActivity destroyed")

        // Reset states when activity is destroyed
        loginViewModel.resetAllStates()
    }
}

/**
 * Login screen Composable wrapper
 */
@Composable
private fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String?) -> Unit,
    onPasswordResetRequest: (String) -> Unit
) {
    // Observe UI states
    val uiState by viewModel.uiState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val resetState by viewModel.resetState.collectAsState()

    // Handle login success
    LaunchedEffect(authState) {
        when (val currentAuthState = authState) {
            is AuthState.Success -> {
                onLoginSuccess(currentAuthState.message)
            }
            else -> {
                // Other states handled by UI
            }
        }
    }

    // Login screen content
    LoginScreenContent(
        uiState = uiState,
        authState = authState,
        resetState = resetState,
        onUsernameChange = viewModel::updateUsername,
        onPasswordChange = viewModel::updatePassword,
        onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
        onRememberMeToggle = viewModel::toggleRememberMe,
        onLoginClick = viewModel::login,
        onPasswordResetRequest = onPasswordResetRequest,
        onClearError = viewModel::resetAuthState
    )
}