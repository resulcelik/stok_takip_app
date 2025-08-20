package com.example.stokkontrolveyonetimsistemi.domain.usecase.auth

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "ResetPasswordUseCase"
    }

    suspend fun sendResetEmail(email: String): Flow<AuthState> {
        Log.d(TAG, "Sending reset email to: ${email.take(3)}***${email.takeLast(4)}")

        return try {
            // Email validation
            if (!isEmailValid(email)) {
                kotlinx.coroutines.flow.flowOf(AuthState.Error("Geçerli bir e-posta adresi girin"))
            } else {
                authRepository.sendResetEmail(email.trim())
            }

        } catch (e: Exception) {
            Log.e(TAG, "Send reset email failed", e)
            kotlinx.coroutines.flow.flowOf(AuthState.Error("E-posta gönderim hatası"))
        }
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ): Flow<AuthState> {
        Log.d(TAG, "Resetting password for: ${email.take(3)}***${email.takeLast(4)}")

        return try {
            // Validation
            when {
                !isEmailValid(email) -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Geçerli bir e-posta adresi girin"))
                }
                !isCodeValid(code) -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("6 haneli doğrulama kodu girin"))
                }
                !isPasswordValid(newPassword) -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Şifre en az 6 karakter olmalıdır"))
                }
                newPassword != confirmPassword -> {
                    kotlinx.coroutines.flow.flowOf(AuthState.Error("Şifreler eşleşmiyor"))
                }
                else -> {
                    authRepository.resetPassword(
                        email.trim(),
                        code.trim(),
                        newPassword,
                        confirmPassword
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Reset password failed", e)
            kotlinx.coroutines.flow.flowOf(AuthState.Error("Şifre sıfırlama hatası"))
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isCodeValid(code: String): Boolean {
        return code.isNotBlank() &&
                code.length == 6 &&
                code.all { it.isDigit() }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }
}