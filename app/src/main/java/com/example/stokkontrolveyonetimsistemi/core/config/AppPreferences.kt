package com.example.stokkontrolveyonetimsistemi.core.config

import androidx.annotation.Keep
import java.util.Date

/**
 * App theme options
 */
enum class AppTheme(val displayName: String) {
    LIGHT("Açık Tema"),
    DARK("Koyu Tema"),
    SYSTEM("Sistem Ayarı")
}

/**
 * App language options
 */
enum class AppLanguage(val displayName: String, val code: String) {
    TURKISH("Türkçe", "tr"),
    ENGLISH("English", "en")
}

/**
 * Dashboard refresh configuration
 */
@Keep
data class RefreshConfig(
    val autoRefreshEnabled: Boolean = true,
    val refreshIntervalMinutes: Int = 5,
    val lastManualRefresh: Date? = null,
    val refreshInProgress: Boolean = false
) {
    /**
     * Check if auto refresh is due
     */
    fun isAutoRefreshDue(): Boolean {
        if (!autoRefreshEnabled) return false

        val lastUpdate = lastManualRefresh ?: Date(0)
        val elapsed = (Date().time - lastUpdate.time) / (1000 * 60) // minutes

        return elapsed >= refreshIntervalMinutes
    }
}

/**
 * User preferences configuration
 */
@Keep
data class UserPreferences(
    // UI preferences
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.TURKISH,

    // Dashboard preferences
    val autoRefreshEnabled: Boolean = true,
    val showQuickStats: Boolean = true,
    val showRecentActivity: Boolean = true,

    // Scanner preferences
    val scanSoundEnabled: Boolean = true,
    val scanVibrationEnabled: Boolean = true,
    val cameraFlashEnabled: Boolean = false,

    // Notification preferences
    val pushNotificationsEnabled: Boolean = true,
    val sessionWarningsEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false
)