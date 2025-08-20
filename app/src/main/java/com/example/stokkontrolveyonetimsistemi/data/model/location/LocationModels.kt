package com.example.stokkontrolveyonetimsistemi.data.model.location

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/**
 * Bölge (Region) DTO
 * GET /api/user/settings/bolge/all response'u
 */
@Keep
data class BolgeDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("bolgeAdi")
    val bolgeAdi: String
)

/**
 * İl (Province) DTO
 * GET /api/user/settings/il/by-parent/{bolgeId} response'u
 */
@Keep
data class IlDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ilAdi")
    val ilAdi: String,

    @SerializedName("bolgeId")
    val bolgeId: Long
)

/**
 * İlçe (District) DTO
 * GET /api/user/settings/ilce/by-parent/{ilId} response'u
 */
@Keep
data class IlceDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("ilceAdi")
    val ilceAdi: String,

    @SerializedName("ilId")
    val ilId: Long
)

/**
 * Depo (Warehouse) DTO
 * GET /api/user/settings/depo/by-parent/{ilceId} response'u
 */
@Keep
data class DepoDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("depoAdi")
    val depoAdi: String,

    @SerializedName("ilceId")
    val ilceId: Long,

    @SerializedName("adres")
    val adres: String,

    @SerializedName("aktif")
    val aktif: Boolean
) {
    /**
     * Is warehouse active/available
     */
    fun isAvailable(): Boolean = aktif
}

// ==========================================
// SESSION MANAGEMENT DTOs
// ==========================================

/**
 * Set Location Request
 * POST /api/user/session/set-location request body
 */
@Keep
data class SetLocationRequest(
    @SerializedName("bolgeId")
    val bolgeId: Long,

    @SerializedName("depoId")
    val depoId: Long
)

// ==========================================
// PRODUCT METADATA DTOs
// ==========================================

/**
 * Stok Birimi (Stock Unit) DTO
 * Stock unit dropdown için kullanılır
 * Ürün ekleme formunda kullanılacak
 */
@Keep
data class StokBirimiDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("stokBirimiAdi")
    val stokBirimiAdi: String,

    @SerializedName("kisaAd")
    val kisaAd: String? = null
) {
    fun getDisplayName(): String = stokBirimiAdi
    fun getShortName(): String = kisaAd ?: stokBirimiAdi
}

// NOTE: Marka DTO'su yok - Marka dropdown olmayacak, null gönderilecek

// ==========================================
// UI STATE MANAGEMENT MODELS
// ==========================================

/**
 * User Location Selection State
 * UI state management için location seçim durumu
 */
data class LocationSelectionState(
    val selectedBolge: BolgeDto? = null,
    val selectedIl: IlDto? = null,
    val selectedIlce: IlceDto? = null,
    val selectedDepo: DepoDto? = null,

    val availableBolgeler: List<BolgeDto> = emptyList(),
    val availableIller: List<IlDto> = emptyList(),
    val availableIlceler: List<IlceDto> = emptyList(),
    val availableDepolar: List<DepoDto> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Is selection complete (Bölge + Depo seçilmiş mi?)
     */
    fun isSelectionComplete(): Boolean {
        return selectedBolge != null && selectedDepo != null
    }

    /**
     * Get selection summary for display
     */
    fun getSelectionSummary(): String? {
        if (!isSelectionComplete()) return null

        return buildString {
            selectedBolge?.let { append(it.bolgeAdi) }
            selectedIl?.let { append(" - ${it.ilAdi}") }
            selectedIlce?.let { append(" - ${it.ilceAdi}") }
            selectedDepo?.let { append(" - ${it.depoAdi}") }
        }
    }

    /**
     * Can select İl (Bölge seçilmiş mi?)
     */
    fun canSelectIl(): Boolean = selectedBolge != null

    /**
     * Can select İlçe (İl seçilmiş mi?)
     */
    fun canSelectIlce(): Boolean = selectedIl != null

    /**
     * Can select Depo (İlçe seçilmiş mi?)
     */
    fun canSelectDepo(): Boolean = selectedIlce != null
}

// ==========================================
// LOCATION OPERATION RESULTS
// ==========================================

/**
 * Location operation result states
 */
sealed class LocationResult<out T> {
    object Loading : LocationResult<Nothing>()
    data class Success<T>(val data: T) : LocationResult<T>()
    data class Error(val message: String, val code: Int? = null) : LocationResult<Nothing>()
    object TokenExpired : LocationResult<Nothing>()
}

/**
 * Location Change State (for UI feedback)
 */
sealed class LocationChangeState {
    object Idle : LocationChangeState()
    object Loading : LocationChangeState()
    data class Success(val message: String) : LocationChangeState()
    data class Error(val message: String) : LocationChangeState()
}