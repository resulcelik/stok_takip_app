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
)
