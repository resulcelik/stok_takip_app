package com.example.stokkontrolveyonetimsistemi.domain.usecase.auth

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import kotlinx.coroutines.flow.Flow

class AutoLoginUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "AutoLoginUseCase"
    }

    suspend fun execute(): Flow<AuthState> {
        Log.d(TAG, "Checking auto login eligibility")

        return try {
            // Check if auto login is enabled
            if (!tokenStorage.isAutoLoginEnabled()) {
                Log.d(TAG, "Auto login disabled")
                kotlinx.coroutines.flow.flowOf(AuthState.Error("Otomatik giriş devre dışı"))
            } else {
                // Use repository's auto login check
                authRepository.checkAutoLogin()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Auto login check failed", e)
            kotlinx.coroutines.flow.flowOf(AuthState.Error("Otomatik giriş kontrolü başarısız"))
        }
    }

    fun getLastLoginInfo(): LastLoginInfo? {
        return try {
            if (tokenStorage.isRememberMeEnabled()) {
                LastLoginInfo(
                    username = tokenStorage.getLastUsername(),
                    rememberMe = true,
                    lastLoginTime = tokenStorage.getLastLoginTime()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last login info", e)
            null
        }
    }
}