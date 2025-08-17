package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

import androidx.annotation.Keep
import java.util.Date

/**
 * Dashboard quick statistics model - CLEANED VERSION
 * Sadece gerçek veriler için kullanılacak (test verileri kaldırıldı)
 */
@Keep
data class QuickStats(
    // Today's scanning statistics (Backend'den gelecek)
    val todayScanned: Int = 0,
    val todayProducts: Int = 0,
    val todayShelf: Int = 0,

    // User statistics (Backend'den gelecek)
    val userScanCount: Int = 0,
    val userLastScan: Date? = null,

    // Data freshness
    val lastUpdated: Date = Date(),
    val dataAge: Long = 0L // milliseconds since last update
) {
    /**
     * Check if data is stale (older than 5 minutes)
     */
    fun isDataStale(): Boolean = dataAge > 5 * 60 * 1000L

    /**
     * Check if user has any activity today
     */
    fun hasActivityToday(): Boolean = todayScanned > 0 || todayProducts > 0 || todayShelf > 0

    /**
     * Get today's total activity count
     */
    fun getTotalTodayActivity(): Int = todayScanned + todayProducts + todayShelf

    /**
     * Format last scan time
     */
    fun getFormattedLastScan(): String? {
        return userLastScan?.let {
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            formatter.format(it)
        }
    }
}