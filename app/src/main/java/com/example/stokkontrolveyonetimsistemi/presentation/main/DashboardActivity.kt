package com.example.stokkontrolveyonetimsistemi.presentation.main

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
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.DashboardModule
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginActivity
import com.example.stokkontrolveyonetimsistemi.presentation.mobile.MobileRegistrationActivity
import com.example.stokkontrolveyonetimsistemi.presentation.settings.SettingsActivity
import com.example.stokkontrolveyonetimsistemi.presentation.rafetiket.RafEtiketActivity
import com.example.stokkontrolveyonetimsistemi.presentation.urun.UrunEtiketActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Enhanced Dashboard Activity
 * Professional enterprise dashboard with complete module navigation
 * ✅ FIXED: Navigation event handling düzeltildi
 */
class DashboardActivity : ComponentActivity() {

    companion object {
        private const val TAG = "DashboardActivity"
    }

    // Dependency injection
    private val tokenStorage: TokenStorage by inject()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Modern back handling - minimize app
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back pressed - minimizing app")
                moveTaskToBack(true)
            }
        })

        Log.d(TAG, "DashboardActivity started")

        // Critical authentication check
        if (!isUserAuthenticated()) {
            Log.w(TAG, "Authentication failed - redirecting to login")
            navigateToLogin("Oturum süreniz dolmuş, lütfen tekrar giriş yapın")
            return
        }

        // Handle welcome messages
        handleIncomingMessages()

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ BASIT VERSİYON: Sadece UI content, navigation Activity'de
                    DashboardScreenContent(
                        state = dashboardViewModel.uiState.collectAsState().value,
                        onAction = dashboardViewModel::handleAction
                    )
                }
            }
        }

        // ✅ KRİTİK: Navigation events'i observe et
        Log.d(TAG, "🔧 INITIALIZING NAVIGATION EVENT OBSERVER")
        observeNavigationEvents()
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
     * Handle welcome/login messages
     */
    private fun handleIncomingMessages() {
        val authMessage = intent.getStringExtra("auth_message")
        val loginMessage = intent.getStringExtra("login_message")

        when {
            !authMessage.isNullOrBlank() -> {
                Log.d(TAG, "Auth message: $authMessage")
                Toast.makeText(this, authMessage, Toast.LENGTH_LONG).show()
            }
            !loginMessage.isNullOrBlank() -> {
                Log.d(TAG, "Login message: $loginMessage")
                Toast.makeText(this, "Hoş geldiniz! $loginMessage", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Hoş geldiniz!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ✅ ENHANCED: Navigation event observer with detailed logging
     */
    private fun observeNavigationEvents() {
        Log.d(TAG, "🔧 SETTING UP NAVIGATION EVENT OBSERVER")

        lifecycleScope.launch {
            Log.d(TAG, "🔧 NAVIGATION EVENT COLLECTOR STARTED")

            dashboardViewModel.navigationEvent.collect { event ->
                Log.d(TAG, "🎯 NAVIGATION EVENT RECEIVED: $event")
                Log.d(TAG, "🎯 EVENT TYPE: ${event::class.simpleName}")

                handleNavigationEvent(event)
            }
        }
    }

    /**
     * ✅ ENHANCED: Navigation event handling with detailed logging
     */
    private fun handleNavigationEvent(event: NavigationEvent) {
        Log.d(TAG, "🔧 HANDLING NAVIGATION EVENT: ${event::class.simpleName}")

        when (event) {
            is NavigationEvent.NavigateToModule -> {
                Log.d(TAG, "📱 MODULE NAVIGATION: ${event.module}")
                handleModuleNavigation(event.module)
            }
            is NavigationEvent.NavigateToLogin -> {
                Log.d(TAG, "🚀 LOGIN NAVIGATION TRIGGERED: ${event.message}")
                navigateToLogin(event.message)
            }
            is NavigationEvent.ShowError -> {
                Log.d(TAG, "❌ ERROR MESSAGE: ${event.message}")
                Toast.makeText(this, event.message, Toast.LENGTH_LONG).show()
            }
            is NavigationEvent.ShowMessage -> {
                Log.d(TAG, "💬 INFO MESSAGE: ${event.message}")
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            is NavigationEvent.ShowSessionWarning -> {
                Log.d(TAG, "⚠️ SESSION WARNING")
                Toast.makeText(this, "Oturum süreniz yakında dolacak", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Enhanced module navigation - ✅ ALL MODULES ACTIVE
     */
    private fun handleModuleNavigation(module: DashboardModule) {
        when (module) {
            DashboardModule.RAF_URETICI -> {
                Log.d(TAG, "Opening ShelfLabelActivity")
                openShelfLabelGenerator()
            }
            DashboardModule.URUN_URETICI -> {
                Log.d(TAG, "Opening ProductGeneratorActivity")
                openProductGenerator()
            }
            DashboardModule.URUN_EKLE -> {
                Log.d(TAG, "Opening ProductAddActivity")
                openProductAdd()
            }
            DashboardModule.SCANNER -> {
                Log.d(TAG, "Opening ScannerActivity")
                openScanner()
            }
            DashboardModule.SETTINGS -> {
                Log.d(TAG, "Opening SettingsActivity")
                openSettingsScreen()
            }
            DashboardModule.PROFILE -> {
                Log.d(TAG, "Opening ProfileActivity")
                openProfile()
            }
        }
    }

    /**
     * ✅ FIXED: Open RAF Etiket Generator - ACTIVE
     */
    private fun openShelfLabelGenerator() {
        try {
            Log.d(TAG, "Launching RafEtiketActivity")

            val intent = Intent(this, RafEtiketActivity::class.java)
            startActivity(intent)

            Log.d(TAG, "RafEtiketActivity launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open RafEtiketActivity", e)
            Toast.makeText(this, "RAF etiket üretimi açılamadı", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openProductGenerator() {
        try {
            val intent = Intent(this, UrunEtiketActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "UrunEtiketActivity launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open UrunEtiketActivity", e)
            Toast.makeText(this, "Ürün Üretici açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open Product Add Activity - ✅ PLACEHOLDER FOR FUTURE
     */
    private fun openProductAdd() {
        try {
            val intent = Intent(this, MobileRegistrationActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "MobileRegistrationActivity launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open MobileRegistrationActivity", e)
            Toast.makeText(this, "Ürün Ekleme açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open Scanner Activity - ✅ PLACEHOLDER FOR FUTURE
     */
    private fun openScanner() {
        Toast.makeText(this, "Barkod Tarayıcı modülü geliştiriliyor", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "SCANNER module requested - will be implemented next")
    }

    /**
     * Open settings screen - ✅ ACTIVE
     */
    private fun openSettingsScreen() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "SettingsActivity launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open SettingsActivity", e)
            Toast.makeText(this, "Ayarlar açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open profile screen - ✅ PLACEHOLDER FOR FUTURE
     */
    private fun openProfile() {
        Toast.makeText(this, "Profil modülü geliştiriliyor", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "PROFILE module requested - will be implemented next")
    }

    /**
     * ✅ ENHANCED: Login navigation with maximum logging
     */
    private fun navigateToLogin(message: String? = null) {
        Log.d(TAG, "🚀 EXECUTING LOGIN NAVIGATION - Message: $message")

        try {
            // Show logout message if provided
            if (!message.isNullOrBlank()) {
                Log.d(TAG, "📢 SHOWING LOGOUT MESSAGE: $message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }

            val intent = Intent(this, LoginActivity::class.java).apply {
                // ✅ PROPER FLAGS: Clear all activities and start fresh
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (!message.isNullOrBlank()) {
                    putExtra("auth_message", message)
                }
            }

            Log.d(TAG, "🏁 STARTING LOGIN ACTIVITY")
            startActivity(intent)

            Log.d(TAG, "🏁 FINISHING DASHBOARD ACTIVITY")
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "💥 LOGIN NAVIGATION FAILED", e)
            Toast.makeText(this, "Yönlendirme hatası", Toast.LENGTH_SHORT).show()

            // Emergency exit
            finish()
        }
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Dashboard resumed")
        dashboardViewModel.onResume()

        // Check authentication on resume
        if (!isUserAuthenticated()) {
            Log.w(TAG, "Authentication lost on resume - redirecting to login")
            navigateToLogin("Oturum süreniz doldu")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Dashboard paused")
        dashboardViewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DashboardActivity destroyed")
    }
}