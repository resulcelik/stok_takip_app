package com.example.stokkontrolveyonetimsistemi.domain.usecase.auth

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Change Password Use Case
 * Şifre değiştirme işlemleri için business logic
 */
class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "ChangePasswordUseCase"
    }

    /**
     * Execute password change
     */
    suspend fun execute(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Flow<AuthState> {
        Log.d(TAG, "Changing password for authenticated user")

        return try {
            // Validation
            when {
                currentPassword.isBlank() -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Mevcut şifre boş olamaz"))
                }
                !isPasswordValid(newPassword) -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Yeni şifre en az 6 karakter olmalıdır"))
                }
                newPassword != confirmPassword -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Yeni şifreler eşleşmiyor"))
                }
                currentPassword == newPassword -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Yeni şifre mevcut şifreden farklı olmalıdır"))
                }
                else -> {
                    authRepository.changePassword(
                        currentPassword,
                        newPassword,
                        confirmPassword
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Change password failed", e)
            kotlinx.coroutines.flow.flowOf(AuthState.Error("Şifre değiştirme hatası"))
        }
    }

    /**
     * Validate password strength
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank() &&
                password.length >= 6 &&
                password.length <= 50
    }
}