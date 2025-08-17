package com.example.stokkontrolveyonetimsistemi.data.model.dashboard

import androidx.annotation.Keep

/**
 * Updated Dashboard navigation modules
 * Yeni iş akışına göre güncellenmiş modüller
 */
@Keep
enum class DashboardModule(
    val title: String,
    val description: String
) {
    // Primary business functions
    RAF_URETICI(
        title = "Raf Numarası Üret",
        description = "Yeni raf numaraları oluştur ve yazdır"
    ),
    URUN_URETICI(
        title = "Ürün Numarası Üret",
        description = "Yeni ürün numaraları oluştur ve yazdır"
    ),
    URUN_EKLE(
        title = "Ürün Ekle",
        description = "Sisteme yeni ürün kaydı ekle"
    ),
    SCANNER(
        title = "Barkod Tarayıcı",
        description = "Kamera ve fiziksel okuyucu ile barkod tarama"
    ),

    // Settings and configuration
    SETTINGS(
        title = "Ayarlar",
        description = "Şifre değiştirme ve lokasyon ayarları"
    ),

    // Profile management
    PROFILE(
        title = "Profil",
        description = "Kullanıcı bilgileri ve oturum yönetimi"
    )
}