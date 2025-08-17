package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

import androidx.annotation.Keep
import java.util.Date

/**
 * User session information model
 * Dashboard'da gösterilecek kullanıcı bilgileri
 */
@Keep
data class UserSession(
    // Basic user info
    val userId: Long,
    val username: String,
    val fullName: String? = null,
    val email: String? = null,

    // Role and permissions
    val role: UserRole = UserRole.USER,
    val permissions: List<String> = emptyList(),

    // Session details
    val loginTime: Date,
    val lastActivity: Date,
    val sessionId: String,

    // Location information (from backend)
    val currentLocation: UserLocation? = null,

    // Scanner preferences (from TokenStorage)
    val scanSoundEnabled: Boolean = true,
    val scanVibrationEnabled: Boolean = true,

    // Statistics
    val sessionStats: SessionStatistics = SessionStatistics()
) {
    /**
     * Get display name for UI
     */
    fun getDisplayName(): String = fullName?.takeIf { it.isNotBlank() } ?: username

    /**
     * Get session duration in minutes
     */
    fun getSessionDuration(): Long {
        return (Date().time - loginTime.time) / (1000 * 60)
    }

    /**
     * Check if user has permission
     */
    fun hasPermission(permission: String): Boolean = permissions.contains(permission)

    /**
     * Get role display name
     */
    fun getRoleDisplayName(): String = role.displayName

    /**
     * Check if session is active (activity within last 30 minutes)
     */
    fun isSessionActive(): Boolean {
        val inactiveTime = (Date().time - lastActivity.time) / (1000 * 60)
        return inactiveTime < 30
    }
}

/**
 * User roles in the system
 */
enum class UserRole(val displayName: String, val level: Int) {
    USER("Kullanıcı", 1),
    SUPERVISOR("Süpervizör", 2),
    MANAGER("Yönetici", 3),
    ADMIN("Admin", 4)
}

/**
 * User location information
 */
@Keep
data class UserLocation(
    val regionId: Long,
    val regionName: String,
    val provinceId: Long,
    val provinceName: String,
    val districtId: Long,
    val districtName: String,
    val warehouseId: Long,
    val warehouseName: String
) {
    /**
     * Get full location string
     */
    fun getFullLocationName(): String = "$warehouseName, $districtName, $provinceName"

    /**
     * Get short location string
     */
    fun getShortLocationName(): String = warehouseName
}

/**
 * Session statistics for current session
 */
@Keep
data class SessionStatistics(
    val scansCount: Int = 0,
    val productsScanned: Int = 0,
    val shelvesScanned: Int = 0,
    val errorsCount: Int = 0,
    val averageScanTime: Double = 0.0,
    val sessionStartTime: Date = Date()
) {
    /**
     * Get scans per minute rate
     */
    fun getScansPerMinute(): Double {
        val sessionMinutes = (Date().time - sessionStartTime.time) / (1000 * 60).toDouble()
        return if (sessionMinutes > 0) scansCount / sessionMinutes else 0.0
    }

    /**
     * Get error rate percentage
     */
    fun getErrorRate(): Double {
        return if (scansCount > 0) (errorsCount.toDouble() / scansCount) * 100 else 0.0
    }

    /**
     * Check if session is productive (more than 10 scans per hour)
     */
    fun isProductiveSession(): Boolean {
        val scansPerHour = getScansPerMinute() * 60
        return scansPerHour > 10
    }
}