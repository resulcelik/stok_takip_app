package com.example.stokkontrolveyonetimsistemi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginActivity
import com.example.stokkontrolveyonetimsistemi.presentation.main.DashboardActivity
import org.koin.android.ext.android.inject

/**
 * MainActivity - Authentication Router
 * Bu Activity sadece authentication durumuna göre yönlendirme yapar
 * Gerçek dashboard functionality DashboardActivity'de bulunur
 *
 * Navigation Flow:
 * SplashActivity → LoginActivity → MainActivity (router) → DashboardActivity
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Dependency injection
    private val tokenStorage: TokenStorage by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity (Router) started")

        // Immediate authentication check and redirect
        if (isUserAuthenticated()) {
            Log.d(TAG, "User authenticated - redirecting to Dashboard")
            navigateToDashboard()
        } else {
            Log.w(TAG, "User not authenticated - redirecting to Login")
            navigateToLogin("Oturum gerekli")
        }

        // Bu activity sadece router, UI göstermeye gerek yok
        finish()
    }

    /**
     * Authentication status check
     */
    private fun isUserAuthenticated(): Boolean {
        return try {
            val isValid = tokenStorage.isTokenValid()

            if (!isValid) {
                Log.d(TAG, "Authentication failed - token invalid or expired")
            } else {
                Log.d(TAG, "Authentication successful - token valid")
            }

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Authentication check failed", e)
            try {
                tokenStorage.clearExpiredToken()
            } catch (cleanupError: Exception) {
                Log.e(TAG, "Token cleanup failed", cleanupError)
            }
            false
        }
    }

    /**
     * Navigate to Dashboard Activity (authenticated users)
     */
    private fun navigateToDashboard() {
        Log.d(TAG, "Navigating to DashboardActivity")

        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Pass any incoming messages
            intent.getStringExtra("auth_message")?.let {
                putExtra("auth_message", it)
            }
            intent.getStringExtra("login_message")?.let {
                putExtra("login_message", it)
            }
        }

        startActivity(intent)
        finish()
    }

    /**
     * Navigate to Login Activity (unauthenticated users)
     */
    private fun navigateToLogin(message: String? = null) {
        Log.d(TAG, "Navigating to LoginActivity")

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (!message.isNullOrBlank()) {
                putExtra("auth_message", message)
            }
        }

        startActivity(intent)
        finish()
    }
}