package com.example.stokkontrolveyonetimsistemi.domain.main

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.UserSession
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.UserRole
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.SessionStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date


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


    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
