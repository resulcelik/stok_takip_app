package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

import androidx.annotation.Keep

/**
 * Updated Dashboard screen UI state
 * Yeni dashboard tasarımı için güncellenmiş state
 */
@Keep
data class DashboardState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUserLoaded: Boolean = false,
    val isStatsLoaded: Boolean = false,

    // Data states
    val userInfo: UserSession? = null,
    val quickStats: QuickStats = QuickStats(),
    val sessionState: SessionValidationState = SessionValidationState.Idle,

    // UI states
    val selectedModule: DashboardModule? = null,
    val showNotifications: Boolean = false,
    val showUserMenu: Boolean = false,
    val navigationEnabled: Boolean = true,

    // Session management
    val tokenExpiryMinutes: Long = 0L,

    // Error handling
    val errorMessage: String? = null,
    val showError: Boolean = false
) {
    /**
     * Check if dashboard is fully loaded
     */
    fun isFullyLoaded(): Boolean = isUserLoaded && isStatsLoaded

    /**
     * Should show session warning
     */
    fun shouldShowSessionWarning(): Boolean {
        return tokenExpiryMinutes in 1..5 &&
                sessionState is SessionValidationState.Warning
    }

    /**
     * Get loading progress (0.0 to 1.0)
     */
    fun getLoadingProgress(): Float {
        var progress = 0f
        if (isUserLoaded) progress += 0.5f
        if (isStatsLoaded) progress += 0.5f
        return progress
    }

    /**
     * Is in error state
     */
    fun isInError(): Boolean = showError && !errorMessage.isNullOrBlank()

    /**
     * Check if user has location set
     */
    fun hasLocationSet(): Boolean = userInfo?.currentLocation != null

    /**
     * Get location status message
     */
    fun getLocationStatusMessage(): String {
        return if (hasLocationSet()) {
            userInfo?.currentLocation?.getFullLocationName() ?: "Lokasyon mevcut"
        } else {
            "Lokasyon giriniz"
        }
    }
}