package com.example.stokkontrolveyonetimsistemi.data.model.auth

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/**
 * JWT Login response
 * POST /api/auth/login response'u
 */
@Keep
data class AuthResponse(
    @SerializedName("token")
    val token: String
) {
    /**
     * Token'ın geçerli olup olmadığını kontrol et
     */
    fun isValidToken(): Boolean {
        return token.isNotBlank() && token.length > 20 // Basit validation
    }
}

/**
 * Generic message response
 * Success/Error mesajları için kullanılır
 */
@Keep
data class MessageResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any? = null
) {
    /**
     * Response başarılı mı?
     */
    fun isSuccess(): Boolean = status in 200..299

}

/**
 * NEW: Generic GET response wrapper
 * Backend'den dönen list ve data response'ları için
 * GET /api/user/settings/bolge/all gibi endpoint'ler için kullanılır
 */
@Keep
data class GetResponse<T>(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T?
) {
    /**
     * Response başarılı mı?
     */
    fun isSuccess(): Boolean = status in 200..299


    /**
     * Safe data access
     */
    fun getDataOrNull(): T? = if (isSuccess()) data else null

}

/**
 * Token validation result
 * Token durumu kontrolü için internal model
 */
sealed class TokenValidationResult {
    object Valid : TokenValidationResult()
    object Expired : TokenValidationResult()
    object Invalid : TokenValidationResult()
    object Missing : TokenValidationResult()
}

/**
 * Authentication states
 * UI state management için
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String? = null) : AuthState()
    data class Error(val message: String, val code: Int? = null) : AuthState()
    object TokenExpired : AuthState()
    object Unauthorized : AuthState()
}