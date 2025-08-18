package com.example.stokkontrolveyonetimsistemi.data.local.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.stokkontrolveyonetimsistemi.core.constants.AppConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.TokenValidationResult
import java.util.*

/**
 * Token expiry information data class
 * ✅ ADDED: Required by AuthRepository.checkAutoLogin()
 */
data class TokenExpiryInfo(
    val expiryTimestamp: Long,
    val timeUntilExpiry: Long,
    val isExpired: Boolean
)

/**
 * Secure JWT token storage with encrypted SharedPreferences
 * JWT token'lar için güvenli depolama ve yönetim sınıfı
 * ✅ COMPATIBLE: Uses existing TokenValidationResult from AuthResponse.kt
 * ✅ NO REDECLARATION: Works with existing model classes
 * ✅ FIXED: All missing methods added
 */
class TokenStorage(private val context: Context) {

    companion object {
        private const val TAG = "TokenStorage"
        private const val KEY_LAST_USERNAME = "last_username" // ✅ ADDED: Missing key
    }

    // Encrypted shared preferences setup
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            AppConstants.SHARED_PREF_NAME, // ✅ USES EXISTING CONSTANT
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ==========================================
    // JWT TOKEN CORE METHODS
    // ==========================================

    /**
     * Save JWT token securely
     */
    fun saveToken(token: String): Boolean {
        return try {
            val expiryTime = System.currentTimeMillis() + AppConstants.JWT_EXPIRY_THRESHOLD_MS

            sharedPreferences.edit()
                .putString(AppConstants.KEY_JWT_TOKEN, token)
                .putLong(AppConstants.KEY_TOKEN_EXPIRY, expiryTime)
                .apply()

            Log.d(TAG, "JWT token saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save JWT token", e)
            false
        }
    }

    /**
     * Get stored JWT token
     */
    fun getToken(): String? {
        return try {
            val token = sharedPreferences.getString(AppConstants.KEY_JWT_TOKEN, null)
            if (token.isNullOrBlank()) {
                Log.d(TAG, "No JWT token found")
                null
            } else {
                Log.d(TAG, "JWT token retrieved successfully")
                token
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get JWT token", e)
            null
        }
    }

    // TokenStorage.kt içine ekle (USER PREFERENCES MANAGEMENT bölümüne)

    /**
     * Get session ID for API calls
     * Token'dan veya username'den session ID oluştur
     */
    fun getSessionId(): String? {
        return try {
            // Önce token'ı kontrol et
            val token = getToken()
            if (!token.isNullOrBlank()) {
                // Token'dan session ID üret (ilk 20 karakter)
                return token.take(20)
            }

            // Token yoksa username'i kullan
            val username = getLastUsername()
            if (username.isNotEmpty()) {
                return "session_$username"
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get session ID", e)
            null
        }
    }

    /**
     * ✅ REQUIRED BY NETWORKMODULE: Check if token exists and is valid
     */
    fun hasValidToken(): Boolean {
        return try {
            val token = getToken()
            val isValid = isTokenValid()

            Log.d(TAG, "Token validation: exists=${!token.isNullOrBlank()}, valid=$isValid")
            !token.isNullOrBlank() && isValid
        } catch (e: Exception) {
            Log.e(TAG, "Token validation failed", e)
            false
        }
    }

    /**
     * Check if current token is still valid (time-based)
     */
    fun isTokenValid(): Boolean {
        return try {
            val token = getToken()
            if (token.isNullOrBlank()) {
                Log.d(TAG, "Token validation failed: no token")
                return false
            }

            val expiryTime = sharedPreferences.getLong(AppConstants.KEY_TOKEN_EXPIRY, 0)
            val currentTime = System.currentTimeMillis()

            val isValid = currentTime < expiryTime
            Log.d(TAG, "Token validation: expires in ${(expiryTime - currentTime) / 1000} seconds")

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Token validation check failed", e)
            false
        }
    }

    /**
     * ✅ COMPATIBLE: Get detailed token validation result using EXISTING enum
     */
    fun getTokenValidationResult(): TokenValidationResult {
        return try {
            val token = getToken()

            when {
                token.isNullOrBlank() -> TokenValidationResult.Missing  // ✅ USES EXISTING
                !isTokenValid() -> TokenValidationResult.Expired        // ✅ USES EXISTING
                else -> TokenValidationResult.Valid                     // ✅ USES EXISTING (object, not data class)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token validation result failed", e)
            TokenValidationResult.Invalid // ✅ USES EXISTING (instead of Error)
        }
    }

    /**
     * Clear expired token
     */
    fun clearExpiredToken(): Boolean {
        return try {
            if (!isTokenValid()) {
                clearToken()
                Log.d(TAG, "Expired token cleared")
                true
            } else {
                Log.d(TAG, "Token still valid, not cleared")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear expired token", e)
            false
        }
    }

    /**
     * Clear all stored token data
     */
    fun clearToken(): Boolean {
        return try {
            sharedPreferences.edit()
                .remove(AppConstants.KEY_JWT_TOKEN)
                .remove(AppConstants.KEY_TOKEN_EXPIRY)
                .remove(AppConstants.KEY_LAST_LOGIN)
                .remove(KEY_LAST_USERNAME) // ✅ ADDED: Clear username too
                .apply()

            Log.d(TAG, "All token data cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear token data", e)
            false
        }
    }

    // ==========================================
    // USER PREFERENCES MANAGEMENT
    // ==========================================

    /**
     * Save user info for auto-login
     * ✅ ENHANCED: Also save username separately
     */
    fun saveUserInfo(username: String): Boolean {
        return try {
            sharedPreferences.edit()
                .putString(AppConstants.KEY_USER_INFO, username)
                .putString(KEY_LAST_USERNAME, username) // ✅ ADDED: Save last username
                .putLong(AppConstants.KEY_LAST_LOGIN, System.currentTimeMillis())
                .apply()

            Log.d(TAG, "User info saved for auto-login")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user info", e)
            false
        }
    }

    /**
     * Get stored username for auto-login
     */
    fun getStoredUsername(): String? {
        return try {
            sharedPreferences.getString(AppConstants.KEY_USER_INFO, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get stored username", e)
            null
        }
    }

    /**
     * ✅ ADDED: Get last username (required by GetUserInfoUseCase)
     */
    fun getLastUsername(): String {
        return try {
            sharedPreferences.getString(KEY_LAST_USERNAME, "") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last username", e)
            ""
        }
    }

    /**
     * Enable/disable auto-login feature
     */
    fun setAutoLoginEnabled(enabled: Boolean): Boolean {
        return try {
            sharedPreferences.edit()
                .putBoolean(AppConstants.KEY_AUTO_LOGIN_ENABLED, enabled)
                .apply()

            Log.d(TAG, "Auto-login ${if (enabled) "enabled" else "disabled"}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set auto-login preference", e)
            false
        }
    }

    /**
     * Check if auto-login is enabled
     */
    fun isAutoLoginEnabled(): Boolean {
        return try {
            sharedPreferences.getBoolean(AppConstants.KEY_AUTO_LOGIN_ENABLED, false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check auto-login preference", e)
            false
        }
    }

    /**
     * ✅ ADDED: Enable/disable remember me feature (required by GetUserInfoUseCase)
     */
    fun setRememberMeEnabled(enabled: Boolean): Boolean {
        return try {
            sharedPreferences.edit()
                .putBoolean(AppConstants.KEY_REMEMBER_ME, enabled)
                .apply()

            Log.d(TAG, "Remember me ${if (enabled) "enabled" else "disabled"}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set remember me preference", e)
            false
        }
    }

    /**
     * ✅ ADDED: Check if remember me is enabled (required by GetUserInfoUseCase)
     */
    fun isRememberMeEnabled(): Boolean {
        return try {
            sharedPreferences.getBoolean(AppConstants.KEY_REMEMBER_ME, false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check remember me preference", e)
            false
        }
    }

    /**
     * ✅ ADDED: Set remember me preference (required by LoginUseCase)
     */
    fun setRememberMe(enabled: Boolean): Boolean {
        return try {
            sharedPreferences.edit()
                .putBoolean(AppConstants.KEY_REMEMBER_ME, enabled)
                .apply()

            Log.d(TAG, "Remember me set to: $enabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set remember me", e)
            false
        }
    }

    /**
     * ✅ ADDED: Set last username (required by LoginUseCase)
     */
    fun setLastUsername(username: String): Boolean {
        return try {
            sharedPreferences.edit()
                .putString(KEY_LAST_USERNAME, username)
                .apply()

            Log.d(TAG, "Last username saved: ${username.take(3)}***")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set last username", e)
            false
        }
    }

    // ==========================================
    // SCANNER PREFERENCES (Future use)
    // ==========================================

    /**
     * Save scanner mode preference
     */
    fun setScannerMode(mode: String): Boolean {
        return try {
            sharedPreferences.edit()
                .putString(AppConstants.KEY_SCANNER_MODE, mode)
                .apply()

            Log.d(TAG, "Scanner mode set to: $mode")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set scanner mode", e)
            false
        }
    }

    /**
     * Get scanner mode preference
     */
    fun getScannerMode(): String {
        return try {
            sharedPreferences.getString(AppConstants.KEY_SCANNER_MODE, AppConstants.SCANNER_MODE_AUTO)
                ?: AppConstants.SCANNER_MODE_AUTO
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get scanner mode, using default", e)
            AppConstants.SCANNER_MODE_AUTO
        }
    }

    /**
     * Enable/disable scan sound
     */
    fun setScanSoundEnabled(enabled: Boolean): Boolean {
        return try {
            sharedPreferences.edit()
                .putBoolean(AppConstants.KEY_SCAN_SOUND_ENABLED, enabled)
                .apply()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set scan sound preference", e)
            false
        }
    }

    /**
     * Check if scan sound is enabled
     */
    fun isScanSoundEnabled(): Boolean {
        return try {
            sharedPreferences.getBoolean(AppConstants.KEY_SCAN_SOUND_ENABLED, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check scan sound preference", e)
            true // Default to enabled
        }
    }

    /**
     * Enable/disable scan vibration
     */
    fun setScanVibrationEnabled(enabled: Boolean): Boolean {
        return try {
            sharedPreferences.edit()
                .putBoolean(AppConstants.KEY_SCAN_VIBRATION_ENABLED, enabled)
                .apply()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set scan vibration preference", e)
            false
        }
    }

    /**
     * Check if scan vibration is enabled
     */
    fun isScanVibrationEnabled(): Boolean {
        return try {
            sharedPreferences.getBoolean(AppConstants.KEY_SCAN_VIBRATION_ENABLED, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check scan vibration preference", e)
            true // Default to enabled
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    /**
     * Get last login timestamp
     */
    fun getLastLoginTime(): Long {
        return try {
            sharedPreferences.getLong(AppConstants.KEY_LAST_LOGIN, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last login time", e)
            0
        }
    }

    /**
     * ✅ ADDED: Set last login time manually (required by SessionValidationUseCase)
     */
    fun setLastLoginTime(timestamp: Long = System.currentTimeMillis()): Boolean {
        return try {
            sharedPreferences.edit()
                .putLong(AppConstants.KEY_LAST_LOGIN, timestamp)
                .apply()

            Log.d(TAG, "Last login time updated to: ${Date(timestamp)}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set last login time", e)
            false
        }
    }

    /**
     * ✅ ADDED: Get token expiry information (required by AuthRepository)
     */
    fun getTokenExpiryInfo(): TokenExpiryInfo? {
        return try {
            val expiryTime = sharedPreferences.getLong(AppConstants.KEY_TOKEN_EXPIRY, 0)
            if (expiryTime > 0) {
                val currentTime = System.currentTimeMillis()
                val timeUntilExpiry = expiryTime - currentTime

                TokenExpiryInfo(
                    expiryTimestamp = expiryTime,
                    timeUntilExpiry = timeUntilExpiry,
                    isExpired = timeUntilExpiry <= 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token expiry info", e)
            null
        }
    }

    /**
     * ✅ ADDED: Get last login time as Date (required by GetUserInfoUseCase)
     */
    fun getLastLoginTimeAsDate(): Date {
        return try {
            val timestamp = getLastLoginTime()
            if (timestamp > 0) Date(timestamp) else Date()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last login time as Date", e)
            Date()
        }
    }

    /**
     * Clear all user data (logout)
     */
    fun clearAllUserData(): Boolean {
        return try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "All user data cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all user data", e)
            false
        }
    }

    /**
     * ✅ ADDED: Clear user session (required by LogoutUseCase)
     */
    fun clearUserSession(): Boolean {
        return try {
            sharedPreferences.edit()
                .remove(AppConstants.KEY_JWT_TOKEN)
                .remove(AppConstants.KEY_TOKEN_EXPIRY)
                .remove(AppConstants.KEY_USER_INFO)
                .remove(KEY_LAST_USERNAME)
                .remove(AppConstants.KEY_LAST_LOGIN)
                .remove(AppConstants.KEY_AUTO_LOGIN_ENABLED)
                .remove(AppConstants.KEY_REMEMBER_ME)
                .apply()

            Log.d(TAG, "User session cleared completely")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user session", e)
            false
        }
    }

    /**
     * ✅ ADDED: Clear user session but keep remember me settings (required by LogoutUseCase)
     */
    fun clearUserSessionKeepRememberMe(): Boolean {
        return try {
            // Get current remember me settings before clearing
            val rememberMe = isRememberMeEnabled()
            val lastUsername = getLastUsername()

            // Clear session data
            sharedPreferences.edit()
                .remove(AppConstants.KEY_JWT_TOKEN)
                .remove(AppConstants.KEY_TOKEN_EXPIRY)
                .remove(AppConstants.KEY_USER_INFO)
                .remove(AppConstants.KEY_LAST_LOGIN)
                .remove(AppConstants.KEY_AUTO_LOGIN_ENABLED)
                .apply()

            // Restore remember me settings if they were enabled
            if (rememberMe && lastUsername.isNotEmpty()) {
                sharedPreferences.edit()
                    .putBoolean(AppConstants.KEY_REMEMBER_ME, true)
                    .putString(KEY_LAST_USERNAME, lastUsername)
                    .apply()
                Log.d(TAG, "User session cleared, remember me preserved")
            } else {
                Log.d(TAG, "User session cleared, no remember me to preserve")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user session while keeping remember me", e)
            false
        }
    }

    /**
     * Get storage summary for debugging
     */
    fun getStorageSummary(): Map<String, String> {
        return try {
            mapOf(
                "hasToken" to (!getToken().isNullOrBlank()).toString(),
                "tokenValid" to isTokenValid().toString(),
                "autoLoginEnabled" to isAutoLoginEnabled().toString(),
                "lastLogin" to Date(getLastLoginTime()).toString(),
                "lastUsername" to getLastUsername(),
                "scannerMode" to getScannerMode(),
                "soundEnabled" to isScanSoundEnabled().toString(),
                "vibrationEnabled" to isScanVibrationEnabled().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get storage summary", e)
            mapOf("error" to e.message.orEmpty())
        }
    }
}