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
     * Check if user has any activity today
     */
    fun hasActivityToday(): Boolean = todayScanned > 0 || todayProducts > 0 || todayShelf > 0
}