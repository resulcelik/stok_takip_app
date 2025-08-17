package com.example.stokkontrolveyonetimsistemi.presentation.main

import androidx.compose.ui.graphics.Color

/**
 * User activity levels for UI display
 */
enum class UserActivityStatus(val displayName: String, val color: Color) {
    VERY_ACTIVE("Çok Aktif", Color(0xFF4CAF50)),    // Green
    ACTIVE("Aktif", Color(0xFF8BC34A)),              // Light Green
    MODERATE("Orta", Color(0xFFFF9800)),             // Orange
    INACTIVE("Pasif", Color(0xFF9E9E9E))             // Grey
}

/**
 * Statistics card configuration for UI
 */
enum class StatsCardType(
    val title: String,
    val valueKey: String,
    val color: Color
) {
    TODAY_SCANNED(
        title = "Bugün Taranan",
        valueKey = "todayScanned",
        color = Color(0xFF1976D2)
    ),
    TOTAL_PRODUCTS(
        title = "Toplam Ürün",
        valueKey = "totalProducts",
        color = Color(0xFF388E3C)
    ),
    ACTIVE_INVENTORY(
        title = "Aktif Sayım",
        valueKey = "activeInventory",
        color = Color(0xFFE64A19)
    ),
    ACCURACY_RATE(
        title = "Doğruluk Oranı",
        valueKey = "accuracyRate",
        color = Color(0xFF7B1FA2)
    )
}