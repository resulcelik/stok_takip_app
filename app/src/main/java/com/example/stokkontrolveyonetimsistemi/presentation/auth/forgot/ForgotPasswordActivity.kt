package com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot

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
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Forgot Password Activity
 * Complete password reset flow:
 * 1. Email input
 * 2. Verification code input
 * 3. New password input
 * 4. Success message
 *
 * ✅ UPDATED: Fixed navigation and back handling
 */
class ForgotPasswordActivity : ComponentActivity() {

    companion object {
        private const val TAG = "ForgotPasswordActivity"

        // Extra keys
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_STEP = "extra_step"
    }

    // ViewModel dependency injection
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "ForgotPasswordActivity started")

        // Get initial email from intent (if provided from login screen)
        val initialEmail = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        val initialStep = intent.getIntExtra(EXTRA_STEP, 1)

        // Set initial email if provided
        if (initialEmail.isNotEmpty()) {
            forgotPasswordViewModel.setEmail(initialEmail)
        }

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ForgotPasswordScreen(
                        viewModel = forgotPasswordViewModel,
                        initialStep = initialStep,
                        onBackToLogin = {
                            navigateBackToLogin()
                        },
                        onPasswordResetSuccess = { message ->
                            handlePasswordResetSuccess(message)
                        }
                    )
                }
            }
        }

        // Observe state changes
        observeViewModelStates()
    }

    /**
     * Handle back button - navigate to login
     * ✅ FIXED: Proper super call order
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateBackToLogin()
    }

    /**
     * Observe ViewModel state changes
     */
    private fun observeViewModelStates() {
        lifecycleScope.launch {
            forgotPasswordViewModel.resetState.collect { resetState ->
                handleResetStateChange(resetState)
            }
        }

        lifecycleScope.launch {
            forgotPasswordViewModel.uiState.collect { uiState ->
                // Handle UI state changes if needed
                if (uiState.isCompleted) {
                    handlePasswordResetSuccess("Şifre başarıyla sıfırlandı")
                }
            }
        }
    }

    /**
     * Handle reset state changes
     */
    private fun handleResetStateChange(resetState: AuthState) {
        when (val currentResetState = resetState) {
            is AuthState.Success -> {
                Log.d(TAG, "Reset operation successful: ${currentResetState.message}")
                showToast(currentResetState.message ?: "İşlem başarılı")
            }

            is AuthState.Error -> {
                Log.w(TAG, "Reset operation failed: ${currentResetState.message}")
                showToast(currentResetState.message)
            }

            is AuthState.Loading -> {
                Log.d(TAG, "Reset operation in progress")
            }

            else -> {
                // Other states handled by UI
            }
        }
    }

    /**
     * Handle successful password reset completion
     * ✅ UPDATED: Clean task stack navigation
     */
    private fun handlePasswordResetSuccess(message: String) {
        Log.d(TAG, "Password reset completed successfully")

        showToast("Şifre başarıyla değiştirildi. Login sayfasına yönlendiriliyorsunuz.")

        // Navigate back to login with success message
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("success_message", message)
        }

        startActivity(intent)
        finish()
    }

    /**
     * Navigate back to login screen
     * ✅ UPDATED: Clean navigation flags
     */
    private fun navigateBackToLogin() {
        Log.d(TAG, "Navigating back to login")

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ForgotPasswordActivity destroyed")

        // Reset ViewModel states
        forgotPasswordViewModel.resetStates()
    }
}

/**
 * Forgot Password Screen Composable wrapper
 */
@Composable
private fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    initialStep: Int = 1,
    onBackToLogin: () -> Unit,
    onPasswordResetSuccess: (String) -> Unit
) {
    // Observe UI states
    val uiState by viewModel.uiState.collectAsState()
    val resetState by viewModel.resetState.collectAsState()

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onPasswordResetSuccess("Şifre başarıyla sıfırlandı")
        }
    }

    // Set initial step
    LaunchedEffect(initialStep) {
        if (initialStep > 1) {
            viewModel.setCurrentStep(initialStep)
        }
    }

    // Forgot password screen content
    ForgotPasswordScreenContent(
        uiState = uiState,
        resetState = resetState,
        onEmailChange = viewModel::setEmail,
        onCodeChange = viewModel::setVerificationCode,
        onNewPasswordChange = viewModel::setNewPassword,
        onConfirmPasswordChange = viewModel::setConfirmPassword,
        onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
        onConfirmPasswordVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
        onSendResetEmail = viewModel::sendResetEmail,
        onVerifyCode = viewModel::verifyCode,
        onResetPassword = viewModel::resetPassword,
        onBackToLogin = onBackToLogin,
        onStepChange = viewModel::setCurrentStep,
        onClearErrors = viewModel::clearErrors
    )
}