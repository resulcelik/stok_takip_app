// data/model/session/UserSessionModels.kt

package com.example.stokkontrolveyonetimsistemi.data.model.session

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * User Session DTO
 * Backend'den gelen kullanıcı session bilgileri
 */
@Keep
data class UserSessionDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("surname")
    val surname: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("aktif")
    val aktif: Boolean,

    // Location fields
    @SerializedName("selectedBolgeId")
    val selectedBolgeId: Long?,

    @SerializedName("selectedDepoId")
    val selectedDepoId: Long?,

    @SerializedName("locationSelectedAt")
    val locationSelectedAt: String?,

    // Transient fields - Backend tarafından doldurulur
    @SerializedName("selectedBolgeAdi")
    val selectedBolgeAdi: String?,

    @SerializedName("selectedDepoAdi")
    val selectedDepoAdi: String?,

    @SerializedName("selectedLokasyonDetay")
    val selectedLokasyonDetay: String?,

    val sessionRemainingMinutes: Int? = null
) {
    /**
     * Get full name
     */
    fun getFullName(): String {
        return when {
            !name.isNullOrBlank() && !surname.isNullOrBlank() -> "$name $surname"
            !name.isNullOrBlank() -> name
            !surname.isNullOrBlank() -> surname
            else -> username
        }
    }

    /**
     * Check if user has location set
     */
    fun hasLocation(): Boolean {
        return selectedBolgeId != null && selectedDepoId != null
    }

    /**
     * Get location display text
     */
    fun getLocationDisplayText(): String {
        return when {
            !selectedLokasyonDetay.isNullOrBlank() -> selectedLokasyonDetay
            !selectedBolgeAdi.isNullOrBlank() && !selectedDepoAdi.isNullOrBlank() ->
                "$selectedBolgeAdi → $selectedDepoAdi"
            !selectedDepoAdi.isNullOrBlank() -> selectedDepoAdi
            else -> "Lokasyon seçilmemiş"
        }
    }
}