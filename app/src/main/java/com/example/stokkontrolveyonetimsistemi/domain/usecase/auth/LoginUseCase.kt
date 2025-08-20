package com.example.stokkontrolveyonetimsistemi.domain.usecase.auth

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import kotlinx.coroutines.flow.Flow

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }

    suspend fun execute(
        username: String,
        password: String,
        rememberMe: Boolean = false
    ): Flow<AuthState> {
        Log.d(TAG, "Login attempt for user: ${username.take(3)}***")

        return try {
            // Validate inputs
            if (username.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Kullanıcı adı ve şifre boş olamaz")
            }

            // Call repository login
            authRepository.login(username, password).also { flow ->
                flow.collect { authState ->
                    // Handle remember me on success
                    if (authState is AuthState.Success && rememberMe) {
                        saveRememberMeInfo(username)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Login use case error", e)
            kotlinx.coroutines.flow.flowOf(AuthState.Error(e.message ?: "Login hatası"))
        }
    }

    private fun saveRememberMeInfo(username: String) {
        try {
            tokenStorage.setRememberMe(true)
            tokenStorage.setLastUsername(username)
            tokenStorage.setAutoLoginEnabled(true)
            tokenStorage.setLastLoginTime()

            Log.d(TAG, "Remember me info saved for user: ${username.take(3)}***")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save remember me info", e)
        }
    }
}

data class LastLoginInfo(
    val username: String,
    val rememberMe: Boolean,
    val lastLoginTime: Long
)