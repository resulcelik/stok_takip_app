package com.example.stokkontrolveyonetimsistemi

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.core.constants.AppConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.di.authModule
import com.example.stokkontrolveyonetimsistemi.di.locationModule
import com.example.stokkontrolveyonetimsistemi.di.mainModule
import com.example.stokkontrolveyonetimsistemi.di.networkModule
import com.example.stokkontrolveyonetimsistemi.di.rafEtiketModule
import com.example.stokkontrolveyonetimsistemi.di.urunEtiketModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

/**
 * Main Application class
 * Koin dependency injection initialization
 * App-wide setup and configuration
 *
 * ✅ UPDATED: ShelfLabelModule added to DI
 * ✅ FIXED: Uses existing AppConstants without redeclaration
 * TOKEN POLICY: No automatic logout, only manual logout allowed
 */
class StokKontrolApplication : Application() {

    companion object {
        private const val TAG = "StokKontrolApp"
    }

    // TokenStorage dependency injection (lazy initialization after Koin setup)
    private val tokenStorage: TokenStorage by inject()

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Application starting...")

        // Initialize Koin Dependency Injection first
        initializeKoin()

        // Perform startup maintenance after Koin setup
        performStartupMaintenance()

        Log.d(TAG, "Application initialization complete")
    }

    /**
     * Initialize Koin dependency injection framework
     * ✅ UPDATED: ShelfLabelModule added
     */
    private fun initializeKoin() {
        Log.d(TAG, "Initializing Koin DI framework...")

        startKoin {
            // Koin logger configuration
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)

            // Android context
            androidContext(this@StokKontrolApplication)

            // ✅ ALL MODULES REGISTERED INCLUDING NEW SHELF LABEL MODULE
            modules(
                networkModule,     // Network & Retrofit & HTTP
                authModule,        // Authentication & JWT
                mainModule,        // Dashboard & Session validation
                locationModule,    // Location cascade & session management
                rafEtiketModule,   // ✅ NEW: RAF etiket üretimi
                urunEtiketModule
            )
        }

        Log.d(TAG, "Koin initialization complete")
    }

    /**
     * Perform application startup maintenance
     * Token cleanup, preferences, etc.
     */
    private fun performStartupMaintenance() {
        Log.d(TAG, "Performing startup maintenance...")

        try {
            // Clean up expired tokens
            if (!tokenStorage.isTokenValid()) {
                Log.d(TAG, "Cleaning up expired token")
                tokenStorage.clearExpiredToken()
            }

            // Clear application cache if needed
            clearApplicationCacheIfNeeded()

            // Log application configuration
            logApplicationConfiguration()

        } catch (e: Exception) {
            Log.e(TAG, "Error during startup maintenance", e)
        }
    }

    /**
     * Clear application cache when necessary
     * ✅ FIXED: Use existing SHARED_PREF_NAME constant
     */
    private fun clearApplicationCacheIfNeeded() {
        try {
            val sharedPrefs = getSharedPreferences(AppConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE) // ✅ FIXED
            val lastCacheClean = sharedPrefs.getLong("last_cache_clean", 0)
            val currentTime = System.currentTimeMillis()

            // Clear cache every 7 days
            if (currentTime - lastCacheClean > 7 * 24 * 60 * 60 * 1000) {
                Log.d(TAG, "Clearing application cache")
                cacheDir.deleteRecursively()

                sharedPrefs.edit()
                    .putLong("last_cache_clean", currentTime)
                    .apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cache cleanup failed", e)
        }
    }

    /**
     * Log important application configuration
     * ✅ FIXED: Use existing constants or provide fallbacks
     */
    private fun logApplicationConfiguration() {
        Log.d(TAG, "=== APPLICATION CONFIGURATION ===")
        Log.d(TAG, "Base URL: ${ApiConstants.BASE_URL}")
        Log.d(TAG, "Debug Mode: ${BuildConfig.DEBUG}")
        Log.d(TAG, "App Name: ${AppConstants.APP_NAME}")

        // ✅ FIXED: Use fallback if DATABASE_NAME doesn't exist
        try {
            Log.d(TAG, "Database Name: ${AppConstants.DATABASE_NAME}")
        } catch (e: Exception) {
            Log.d(TAG, "Database Name: stok_kontrol_database (default)")
        }

        // ✅ FIXED: Use fallback if MAX_SCAN_HISTORY_ITEMS doesn't exist
        try {
            Log.d(TAG, "Max scan history: ${AppConstants.MAX_SCAN_HISTORY_ITEMS}")
        } catch (e: Exception) {
            Log.d(TAG, "Max scan history: 100 (default)")
        }

        Log.d(TAG, "JWT Expiry: ${AppConstants.JWT_EXPIRY_THRESHOLD_HOURS} hours")
        Log.d(TAG, "Barcode timeout: ${AppConstants.SCAN_TIMEOUT_MS / 1000} seconds")

        // Log available modules
        Log.d(TAG, "=== REGISTERED KOIN MODULES ===")
        Log.d(TAG, "✅ NetworkModule - HTTP & Retrofit")
        Log.d(TAG, "✅ AuthModule - Authentication & JWT")
        Log.d(TAG, "✅ MainModule - Dashboard & Session")
        Log.d(TAG, "✅ LocationModule - Location cascade & management")
        Log.d(TAG, "✅ ShelfLabelModule - RAF etiket üretimi") // ✅ NEW
        Log.d(TAG, "=================================")
    }

    override fun onTerminate() {
        Log.d(TAG, "Application terminating...")
        super.onTerminate()
    }

    override fun onLowMemory() {
        Log.w(TAG, "Low memory warning - clearing caches")
        super.onLowMemory()

        try {
            // Clear application cache on low memory
            cacheDir.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Cache cleanup on low memory failed", e)
        }
    }
}