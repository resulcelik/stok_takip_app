// Enhanced GetUserInfoUseCase.kt - Token access için güncellendi

package com.example.stokkontrolveyonetimsistemi.domain.main

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.UserSession
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.UserRole
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.SessionStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Enhanced Get User Information Use Case
 * Token'dan kullanıcı bilgilerini çıkarır ve UserSession oluşturur
 * ✅ ENHANCED: Token access için public property eklendi
 */
class GetUserInfoUseCase(
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "GetUserInfoUseCase"
    }

    /**
     * Execute user information retrieval
     */
    suspend fun execute(): Flow<Result<UserSession>> = flow {
        try {
            Log.d(TAG, "Getting user information from token")

            // Token'ı kontrol et
            if (!tokenStorage.isTokenValid()) {
                emit(Result.failure(Exception("Token geçersiz veya bulunamadı")))
                return@flow
            }

            // Kullanıcı bilgilerini oluştur (token'dan parse edilecek)
            val userSession = createUserSessionFromToken()

            Log.d(TAG, "User session created for: ${userSession.getDisplayName()}")
            emit(Result.success(userSession))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user info", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Create user session from stored token
     * TODO: JWT token'ı parse ederek gerçek kullanıcı bilgilerini çıkar
     */
    private fun createUserSessionFromToken(): UserSession {
        val lastUsername = tokenStorage.getLastUsername()
        val loginTime = tokenStorage.getLastLoginTime()

        return UserSession(
            userId = 1L, // TODO: Token'dan parse et
            username = lastUsername.ifEmpty { "user" },
            fullName = null, // TODO: Token'dan parse et
            email = null, // TODO: Token'dan parse et
            role = UserRole.USER, // TODO: Token'dan parse et
            permissions = emptyList(), // TODO: Token'dan parse et
            loginTime = if (loginTime > 0) Date(loginTime) else Date(),
            lastActivity = Date(),
            sessionId = generateSessionId(),
            currentLocation = null, // TODO: API'den çek
            scanSoundEnabled = tokenStorage.isScanSoundEnabled(),
            scanVibrationEnabled = tokenStorage.isScanVibrationEnabled(),
            sessionStats = SessionStatistics()
        )
    }

    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * ✅ LOGOUT TEST: Get detailed token information
     */
    fun getDetailedTokenInfo(): TokenDebugInfo {
        return try {
            val token = tokenStorage.getToken()
            val expiryInfo = tokenStorage.getTokenExpiryInfo()

            TokenDebugInfo(
                hasToken = !token.isNullOrBlank(),
                tokenLength = token?.length ?: 0,
                isTokenValid = tokenStorage.isTokenValid(),
                expiryInfo = expiryInfo,
                lastUsername = tokenStorage.getLastUsername(),
                rememberMeEnabled = tokenStorage.isRememberMeEnabled(),
                autoLoginEnabled = tokenStorage.isAutoLoginEnabled(),
                lastLoginTime = tokenStorage.getLastLoginTime()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token debug info", e)
            TokenDebugInfo()
        }
    }
}

/**
 * ✅ Token debug information for testing
 */
data class TokenDebugInfo(
    val hasToken: Boolean = false,
    val tokenLength: Int = 0,
    val isTokenValid: Boolean = false,
    val expiryInfo: com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenExpiryInfo? = null,
    val lastUsername: String = "",
    val rememberMeEnabled: Boolean = false,
    val autoLoginEnabled: Boolean = false,
    val lastLoginTime: Long = 0L
) {
    fun toLogString(): String {
        return """
            TOKEN DEBUG INFO:
            - Has Token: $hasToken (Length: $tokenLength)
            - Token Valid: $isTokenValid
            - Remember Me: $rememberMeEnabled
            - Auto Login: $autoLoginEnabled
            - Last User: ${if (lastUsername.isNotEmpty()) "${lastUsername.take(3)}***" else "None"}
            - Last Login: ${if (lastLoginTime > 0) Date(lastLoginTime) else "Never"}
            - Expiry Info: ${expiryInfo?.let { "Expires: ${!it.isExpired}, Time Left: ${it.timeUntilExpiry / (1000 * 60)} min" } ?: "None"}
        """.trimIndent()
    }
}