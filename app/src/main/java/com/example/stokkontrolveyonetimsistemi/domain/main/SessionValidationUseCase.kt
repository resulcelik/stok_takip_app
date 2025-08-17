package com.example.stokkontrolveyonetimsistemi.domain.main

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.SessionInfo
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.SessionValidationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Session Validation Use Case
 * JWT token validation ve session monitoring
 * 🔧 FIXED: Crash-safe monitoring with proper exception handling
 */
class SessionValidationUseCase(
    private val tokenStorage: TokenStorage
) {

    companion object {
        private const val TAG = "SessionValidationUseCase"
        private const val WARNING_THRESHOLD_MINUTES = 5L
    }

    /**
     * Execute session validation
     * 🔧 FIXED: Exception-safe validation
     */
    suspend fun execute(): Flow<SessionValidationState> = flow {
        try {
            Log.d(TAG, "Validating user session")

            val isValid = tokenStorage.isTokenValid()

            if (!isValid) {
                Log.w(TAG, "Token is invalid or expired")
                emit(SessionValidationState.Expired)
                return@flow
            }

            val expiryInfo = tokenStorage.getTokenExpiryInfo()

            if (expiryInfo == null) {
                Log.w(TAG, "Cannot get token expiry info")
                emit(SessionValidationState.Error("Token bilgisi alınamadı"))
                return@flow
            }

            val minutesLeft = expiryInfo.timeUntilExpiry / (1000 * 60)

            when {
                minutesLeft <= 0 -> {
                    Log.w(TAG, "Token expired: $minutesLeft minutes")
                    emit(SessionValidationState.Expired)
                }
                minutesLeft <= WARNING_THRESHOLD_MINUTES -> {
                    Log.w(TAG, "Session warning: $minutesLeft minutes left")
                    emit(SessionValidationState.Warning(minutesLeft))
                }
                else -> {
                    Log.d(TAG, "Session valid: $minutesLeft minutes left")
                    emit(SessionValidationState.Valid)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Session validation failed", e)
            emit(SessionValidationState.Error("Oturum doğrulama hatası: ${e.localizedMessage}"))
        }
    }

    /**
     * Get session information
     * 🔧 FIXED: Safe info retrieval
     */
    fun getSessionInfo(): SessionInfo {
        return try {
            val expiryInfo = tokenStorage.getTokenExpiryInfo()
            val isValid = tokenStorage.isTokenValid()

            SessionInfo(
                isValid = isValid,
                minutesUntilExpiry = if (expiryInfo != null && isValid) {
                    maxOf(0, expiryInfo.timeUntilExpiry / (1000 * 60))
                } else 0,
                lastActivity = Date(), // Current time as last activity
                warningThreshold = WARNING_THRESHOLD_MINUTES
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get session info", e)
            SessionInfo()
        }
    }

    /**
     * Start session monitoring with callbacks
     * 🔧 REMOVED: Bu method'u kaldırıyoruz çünkü ViewModel'de safe monitoring var
     * ViewModel kendi monitoring'ini yapacak
     */
    @Deprecated("Use ViewModel-based monitoring instead", ReplaceWith("ViewModel.startSafeSessionMonitoring()"))
    suspend fun startSessionMonitoring(
        intervalMinutes: Long = 1L,
        onSessionWarning: (Long) -> Unit = {},
        onSessionExpired: () -> Unit = {}
    ): Flow<SessionValidationState> = flow {
        // Bu method artık kullanılmayacak
        Log.w(TAG, "DEPRECATED: Use ViewModel-based monitoring instead")
        emit(SessionValidationState.Error("Use ViewModel monitoring"))
    }

    /**
     * Update last activity timestamp
     * 🔧 FIXED: Safe activity update
     */
    fun updateLastActivity() {
        try {
            // Store current timestamp as last activity
            tokenStorage.setLastLoginTime()
            Log.d(TAG, "Last activity updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update last activity", e)
        }
    }
}