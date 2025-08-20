package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.auth.*
import com.example.stokkontrolveyonetimsistemi.data.network.auth.api.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(username: String, password: String): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)

            Log.d(TAG, "Login attempt for user: $username")

            val request = LoginRequest(
                username = username.trim(),
                password = password
            )

            val response = authApiService.login(request)

            when {
                response.isSuccessful -> {
                    val authResponse = response.body()

                    if (authResponse != null && authResponse.isValidToken()) {
                        // JWT token'ı güvenli olarak kaydet
                        val saved = tokenStorage.saveToken(authResponse.token)

                        if (saved) {
                            Log.d(TAG, "Login successful, token saved")
                            emit(AuthState.Success("Giriş başarılı"))
                        } else {
                            Log.e(TAG, "Token save failed")
                            emit(AuthState.Error("Token kaydedilemedi"))
                        }
                    } else {
                        Log.e(TAG, "Invalid token received")
                        emit(AuthState.Error("Geçersiz token"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Login failed - Invalid credentials")
                    emit(AuthState.Error("Kullanıcı adı veya şifre hatalı"))
                }

                response.code() >= 500 -> {
                    Log.e(TAG, "Server error: ${response.code()}")
                    emit(AuthState.Error("Sunucu hatası, lütfen daha sonra tekrar deneyin"))
                }

                else -> {
                    Log.e(TAG, "Login failed: ${response.code()} - ${response.message()}")
                    emit(AuthState.Error("Giriş işlemi başarısız: ${response.message()}"))
                }
            }

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Login timeout", e)
            emit(AuthState.Error("Bağlantı zaman aşımı"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error during login", e)
            emit(AuthState.Error("İnternet bağlantısı bulunamadı"))
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during login", e)
            handleHttpException(e)?.let { emit(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during login", e)
            emit(AuthState.Error("Beklenmeyen hata oluştu"))
        }
    }


    suspend fun checkAutoLogin(): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)

            Log.d(TAG, "Checking auto-login eligibility")

            // Token storage'dan bilgileri al
            val token = tokenStorage.getToken()
            val isTokenValid = tokenStorage.isTokenValid()
            val expiryInfo = tokenStorage.getTokenExpiryInfo()

            when {
                token.isNullOrBlank() -> {
                    Log.d(TAG, "No token found - manual login required")
                    emit(AuthState.Error("Token bulunamadı"))
                }

                !isTokenValid -> {
                    Log.d(TAG, "Token expired - clearing and requiring manual login")
                    tokenStorage.clearExpiredToken()
                    emit(AuthState.TokenExpired)
                }

                expiryInfo?.timeUntilExpiry != null && expiryInfo.timeUntilExpiry < 0 -> {
                    Log.d(TAG, "Token expired (time check) - clearing")
                    tokenStorage.clearExpiredToken()
                    emit(AuthState.TokenExpired)
                }

                else -> {
                    // Token geçerli - backend'e doğrulama isteği gönder
                    try {
                        val response = authApiService.validateToken()

                        if (response.isSuccessful) {
                            val timeRemaining = expiryInfo?.timeUntilExpiry?.div(1000 * 60) ?: 0
                            Log.d(TAG, "Auto-login successful - $timeRemaining minutes remaining")
                            emit(AuthState.Success("Otomatik giriş başarılı"))
                        } else if (response.code() == ApiConstants.HTTP_UNAUTHORIZED) {
                            Log.d(TAG, "Token invalidated by server")
                            tokenStorage.clearExpiredToken()
                            emit(AuthState.TokenExpired)
                        } else {
                            Log.w(TAG, "Token validation failed: ${response.code()}")
                            emit(AuthState.Error("Token doğrulaması başarısız"))
                        }
                    } catch (e: Exception) {
                        // Network hatası - token'ı koru ama manuel giriş iste
                        Log.w(TAG, "Token validation network error", e)
                        emit(AuthState.Error("Bağlantı hatası - manuel giriş gerekli"))
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Auto-login check failed", e)
            emit(AuthState.Error("Otomatik giriş kontrolü başarısız"))
        }
    }


    suspend fun sendResetEmail(email: String): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)

            Log.d(TAG, "Sending password reset email to: $email")

            val response = authApiService.sendResetEmail(email.trim())

            if (response.isSuccessful) {
                val messageResponse = response.body()
                Log.d(TAG, "Reset email sent successfully")
                emit(AuthState.Success(messageResponse?.message ?: "Şifre sıfırlama e-postası gönderildi"))
            } else {
                Log.e(TAG, "Send reset email failed: ${response.code()}")
                val errorMessage = when (response.code()) {
                    404 -> "E-posta adresi bulunamadı"
                    429 -> "Çok fazla deneme, lütfen bekleyin"
                    else -> "E-posta gönderilemedi"
                }
                emit(AuthState.Error(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Send reset email error", e)
            emit(AuthState.Error(handleNetworkError(e)))
        }
    }


    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)

            Log.d(TAG, "Resetting password for email: $email")

            val request = ResetPasswordRequest(
                email = email.trim(),
                code = code.trim(),
                newPassword = newPassword,
                newPasswordConfirm = confirmPassword
            )

            val response = authApiService.resetPassword(request)

            if (response.isSuccessful) {
                val messageResponse = response.body()
                Log.d(TAG, "Password reset successful")
                emit(AuthState.Success(messageResponse?.message ?: "Şifre başarıyla sıfırlandı"))
            } else {
                Log.e(TAG, "Password reset failed: ${response.code()}")
                val errorMessage = when (response.code()) {
                    400 -> "Geçersiz doğrulama kodu"
                    410 -> "Doğrulama kodu süresi dolmuş"
                    422 -> "Şifre gereksinimleri karşılanmıyor"
                    else -> "Şifre sıfırlama başarısız"
                }
                emit(AuthState.Error(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Reset password error", e)
            emit(AuthState.Error(handleNetworkError(e)))
        }
    }


    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Flow<AuthState> = flow {
        try {
            emit(AuthState.Loading)

            Log.d(TAG, "Changing password for authenticated user")

            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                newPasswordConfirm = confirmPassword
            )

            val response = authApiService.changePassword(request)

            when {
                response.isSuccessful -> {
                    val messageResponse = response.body()
                    Log.d(TAG, "Password change successful")
                    emit(AuthState.Success(messageResponse?.message ?: "Şifre başarıyla değiştirildi"))
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Password change failed - token expired")
                    tokenStorage.clearExpiredToken()
                    emit(AuthState.TokenExpired)
                }

                response.code() == 400 -> {
                    Log.e(TAG, "Password change failed - bad request")
                    emit(AuthState.Error("Mevcut şifre hatalı veya yeni şifre gereksinimlerini karşılamıyor"))
                }

                else -> {
                    Log.e(TAG, "Password change failed: ${response.code()}")
                    emit(AuthState.Error("Şifre değiştirme başarısız"))
                }
            }

        } catch (e: HttpException) {
            if (e.code() == ApiConstants.HTTP_UNAUTHORIZED) {
                tokenStorage.clearExpiredToken()
                emit(AuthState.TokenExpired)
            } else {
                emit(AuthState.Error(handleNetworkError(e)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Change password error", e)
            emit(AuthState.Error(handleNetworkError(e)))
        }
    }

    private fun handleHttpException(exception: HttpException): AuthState? {
        return when (exception.code()) {
            ApiConstants.HTTP_UNAUTHORIZED -> {
                tokenStorage.clearExpiredToken()
                AuthState.TokenExpired
            }
            ApiConstants.HTTP_FORBIDDEN -> AuthState.Error("Bu işlem için yetkiniz bulunmuyor")
            ApiConstants.HTTP_NOT_FOUND -> AuthState.Error("İstenen kaynak bulunamadı")
            ApiConstants.HTTP_INTERNAL_SERVER_ERROR -> AuthState.Error("Sunucu hatası")
            else -> AuthState.Error("İşlem başarısız: ${exception.message()}")
        }
    }

    private fun handleNetworkError(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> "Bağlantı zaman aşımı"
            is IOException -> "İnternet bağlantısı bulunamadı"
            is HttpException -> "Sunucu hatası (${exception.code()})"
            else -> "Beklenmeyen hata oluştu"
        }
    }
}