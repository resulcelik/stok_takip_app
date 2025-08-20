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
     * Get role display name
     */
    fun getRoleDisplayName(): String = role.displayName


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
)
