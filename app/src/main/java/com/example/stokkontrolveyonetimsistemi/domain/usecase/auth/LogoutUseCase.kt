package com.example.stokkontrolveyonetimsistemi.domain.usecase.auth

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutUseCase(
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }

    suspend fun execute(): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)
            Log.d(TAG, "Starting fast logout process")

            val currentUser = tokenStorage.getLastUsername().takeIf { it.isNotEmpty() } ?: "Unknown"
            Log.d(TAG, "Logging out user: ${currentUser.take(3)}***")

            // ✅ DIREKT TOKEN CLEANUP
            performTokenCleanup()
            performSecurityCleanup()

            Log.d(TAG, "✅ Fast logout completed successfully")
            emit(AuthState.Success("Çıkış yapıldı"))

        } catch (e: Exception) {
            Log.e(TAG, "Logout process failed", e)

            // ✅ EMERGENCY: Force cleanup on any error
            try {
                tokenStorage.clearExpiredToken()
                tokenStorage.clearUserSession()
            } catch (cleanupError: Exception) {
                Log.e(TAG, "Emergency cleanup failed", cleanupError)
            }

            emit(AuthState.Success("Çıkış tamamlandı"))
        }
    }

    private fun performTokenCleanup() {
        try {
            Log.d(TAG, "Performing token cleanup")

            // Clear token and session data
            tokenStorage.clearExpiredToken()

            // Clear user session but keep remember me settings if enabled
            if (tokenStorage.isRememberMeEnabled()) {
                tokenStorage.clearUserSessionKeepRememberMe()
                Log.d(TAG, "Session cleared, remember me settings preserved")
            } else {
                tokenStorage.clearUserSession()
                Log.d(TAG, "Complete session cleared")
            }

            Log.d(TAG, "Token cleanup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Token cleanup failed", e)
            throw e
        }
    }

    private fun performSecurityCleanup() {
        try {
            Log.d(TAG, "Performing security cleanup")

            // Disable auto-login for security
            tokenStorage.setAutoLoginEnabled(false)

            // Log security event
            Log.d(TAG, "Security logout completed at: ${java.util.Date()}")

        } catch (e: Exception) {
            Log.e(TAG, "Security cleanup failed", e)
        }
    }
}