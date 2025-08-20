package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

import java.util.Date

/**
 * Session validation states
 * Oturum doğrulama durumları
 */
sealed class SessionValidationState {
    object Idle : SessionValidationState()
    object Loading : SessionValidationState()
    object Valid : SessionValidationState()
    data class Warning(val minutesLeft: Long) : SessionValidationState()
    object Expired : SessionValidationState()
    data class Error(val message: String) : SessionValidationState()
}

/**
 * Session information data class
 * Oturum bilgi modeli
 */
data class SessionInfo(
    val isValid: Boolean = false,
    val minutesUntilExpiry: Long = 0L,
    val lastActivity: Date? = null,
    val warningThreshold: Long = 5L // minutes
)