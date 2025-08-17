package com.example.stokkontrolveyonetimsistemi.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.stokkontrolveyonetimsistemi.presentation.location.LocationActivity
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Settings Activity - Clean Architecture
 * Pure Activity container with LocationActivity integration
 *
 * Dosya Konumu: /presentation/settings/SettingsActivity.kt
 * Pattern: Clean separation - Activity için sadece navigation ve lifecycle
 * UI Logic: SettingsScreenContent.kt'de
 * Business Logic: SettingsViewModel.kt'de
 */
class SettingsActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SettingsActivity"
    }

    // ==========================================
    // DEPENDENCY INJECTION
    // ==========================================

    /**
     * Settings ViewModel injection
     * Existing SettingsViewModel from MainModule.kt kullanıyoruz
     */
    private val settingsViewModel: SettingsViewModel by viewModel()

    // ==========================================
    // LOCATION ACTIVITY RESULT HANDLING
    // ==========================================

    /**
     * Location Activity result launcher
     * LocationActivity'den gelen sonuçları handle eder
     */
    private val locationActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            LocationActivity.RESULT_LOCATION_CHANGED -> {
                val successMessage = result.data?.getStringExtra(LocationActivity.EXTRA_SUCCESS_MESSAGE)
                val locationSummary = result.data?.getStringExtra(LocationActivity.EXTRA_LOCATION_SUMMARY)

                Log.d(TAG, "Location changed successfully: $locationSummary")

                // Show success message to user
                if (!successMessage.isNullOrBlank()) {
                    Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                }

                // Optional: Notify ViewModel about location change
                // settingsViewModel.onLocationUpdateSuccess(locationSummary ?: "")

            }

            RESULT_CANCELED -> {
                Log.d(TAG, "Location selection cancelled by user")
            }

            else -> {
                Log.w(TAG, "Unexpected result code from LocationActivity: ${result.resultCode}")
            }
        }
    }

    // ==========================================
    // LIFECYCLE METHODS
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "SettingsActivity created")

        enableEdgeToEdge()

        // Setup ViewModel observers
        setupViewModelObservers()

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ CLEAN: SettingsScreenContent kullanımı
                    SettingsScreenContent(
                        onBackClick = {
                            finish()
                        },
                        onPasswordChangeSuccess = { message ->
                            // UI'da zaten Toast gösterildi
                            Log.d(TAG, "Password change completed: $message")
                        },
                        onLocationChangeClick = {
                            // ✅ NEW: LocationActivity navigation
                            openLocationActivity()
                        },
                        settingsViewModel = settingsViewModel  // Pass ViewModel to UI
                    )
                }
            }
        }
    }

    /**
     * Setup ViewModel observers
     * ViewModel state changes'leri dinle
     */
    private fun setupViewModelObservers() {
        lifecycleScope.launch {
            // Password change state observer
            settingsViewModel.passwordChangeState.collect { state ->
                when (state) {
                    is PasswordChangeState.Success -> {
                        Toast.makeText(this@SettingsActivity, state.message, Toast.LENGTH_LONG).show()
                        settingsViewModel.resetPasswordChangeState()
                    }
                    is PasswordChangeState.Error -> {
                        Toast.makeText(this@SettingsActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // Loading ve Idle states UI'da handle ediliyor
                    }
                }
            }
        }
    }

    // ==========================================
    // NAVIGATION METHODS
    // ==========================================

    /**
     * Open Location Activity
     * ✅ NEW: Gerçek cascade dropdown ekranını aç
     */
    private fun openLocationActivity() {
        try {
            Log.d(TAG, "Opening LocationActivity for cascade dropdown selection")

            val intent = Intent(this, LocationActivity::class.java)
            locationActivityLauncher.launch(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to open LocationActivity", e)
            Toast.makeText(this, "Lokasyon ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SettingsActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SettingsActivity paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SettingsActivity destroyed")
    }
}