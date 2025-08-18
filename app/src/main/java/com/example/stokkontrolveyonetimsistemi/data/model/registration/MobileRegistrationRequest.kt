package com.example.stokkontrolveyonetimsistemi.data.model.registration

import androidx.annotation.Keep
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunBilgileri
import com.google.gson.annotations.SerializedName

/**
 * Mobile Complete Registration Request
 * POST /api/user/main/mobile/complete-registration
 * Tüm kayıt verilerini içeren ana request modeli
 */
@Keep
data class MobileRegistrationRequest(
    @SerializedName("rafSeriNo")
    val rafSeriNo: String,

    @SerializedName("createNewRaf")
    val createNewRaf: Boolean = true,

    @SerializedName("urunBilgileri")
    val urunBilgileri: UrunBilgileri,

    @SerializedName("tempPhotoIds")
    val tempPhotoIds: List<String>
) {
    /**
     * Validation - Tüm zorunlu alanlar
     */
    fun isValid(): Boolean {
        return rafSeriNo.isNotBlank() &&
                urunBilgileri.isValid() &&
                tempPhotoIds.size >= 4  // Minimum 4 fotoğraf zorunlu
    }

    /**
     * RAF numarası format kontrolü
     */
    fun isRafFormatValid(): Boolean {
        // R ile başlamalı ve 12 karakter olmalı
        return rafSeriNo.matches(Regex("^R\\d{11}$"))
    }
}