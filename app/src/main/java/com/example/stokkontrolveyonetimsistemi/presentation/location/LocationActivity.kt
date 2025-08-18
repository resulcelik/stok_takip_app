package com.example.stokkontrolveyonetimsistemi.presentation.location

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Location Selection Activity
 * 4-seviye cascade dropdown ekranı
 *
 * Dosya Konumu: /presentation/location/LocationActivity.kt
 * Pattern: DashboardActivity.kt ve SettingsActivity.kt pattern'ını takip ediyor
 * Features: Cascade dropdowns, real-time API, modern Material3 UI
 */
class LocationActivity : ComponentActivity() {

    companion object {
        private const val TAG = "LocationActivity"

        // Intent result codes
        const val RESULT_LOCATION_CHANGED = RESULT_FIRST_USER + 1
        const val EXTRA_LOCATION_SUMMARY = "location_summary"
        const val EXTRA_SUCCESS_MESSAGE = "success_message"
    }

    // Dependency injection
    private val locationViewModel: LocationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "LocationActivity created")

        enableEdgeToEdge()

        // Setup navigation event observer
        setupNavigationObserver()

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationScreen()
                }
            }
        }
    }

    // ==========================================
    // NAVIGATION EVENT HANDLING
    // ==========================================

    /**
     * Setup navigation event observer
     * ViewModel navigation events'leri dinle
     */
    private fun setupNavigationObserver() {
        lifecycleScope.launch {
            locationViewModel.navigationEvent.collect { event ->
                when (event) {
                    is LocationNavigationEvent.NavigateBack -> {
                        Log.d(TAG, "Navigate back requested")
                        finish()
                    }

                    is LocationNavigationEvent.NavigateBackWithSuccess -> {
                        Log.d(TAG, "Navigate back with success: ${event.message}")

                        // Set result data for calling activity
                        val resultIntent = android.content.Intent().apply {
                            putExtra(EXTRA_SUCCESS_MESSAGE, event.message)
                            putExtra(EXTRA_LOCATION_SUMMARY, event.locationSummary)
                        }
                        setResult(RESULT_LOCATION_CHANGED, resultIntent)

                        // Show success message
                        finish()
                    }

                    is LocationNavigationEvent.NavigateToLogin -> {
                        Log.w(TAG, "Token expired - navigating to login")
                        navigateToLogin()
                    }

                    is LocationNavigationEvent.ShowError -> {
                        Log.e(TAG, "Error event: ${event.message}")
                    }
                }
            }
        }
    }

    // ==========================================
    // MAIN UI CONTENT
    // ==========================================

    /**
     * Main location screen content
     * Ana lokasyon seçim ekranı
     */
    @Composable
    private fun LocationScreen() {
        val uiState by locationViewModel.uiState.collectAsState()

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            LocationScreenContent(
                state = uiState,
                onBolgeSelected = locationViewModel::onBolgeSelected,
                onIlSelected = locationViewModel::onIlSelected,
                onIlceSelected = locationViewModel::onIlceSelected,
                onDepoSelected = locationViewModel::onDepoSelected,
                onSaveLocation = locationViewModel::saveLocationSelection,
                onResetSelection = locationViewModel::resetSelection,
                onBackPressed = locationViewModel::onBackPressed,
                onErrorDismissed = locationViewModel::clearError,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // ==========================================
    // NAVIGATION HELPERS
    // ==========================================

    /**
     * Navigate to login screen
     * Token süresi dolduğunda login'e yönlendir
     */
    private fun navigateToLogin() {
        try {
            val intent = android.content.Intent(this, LoginActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to login", e)
            finish()
        }
    }

    // ==========================================
    // ACTIVITY LIFECYCLE
    // ==========================================

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LocationActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "LocationActivity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LocationActivity destroyed")
    }

    /**
     * Handle system back button
     * Sistem geri butonunu handle et
     */
    override fun onBackPressed() {
        Log.d(TAG, "System back button pressed")
        locationViewModel.onBackPressed()
    }
}