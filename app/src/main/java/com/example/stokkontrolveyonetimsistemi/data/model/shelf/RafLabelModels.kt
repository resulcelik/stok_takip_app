package com.example.stokkontrolveyonetimsistemi.data.model.shelf

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * RAF Etiket Modelleri - SadeleÅŸtirilmiÅŸ
 * Sadece adet girip STANDART boyutta etiket basma iÃ§in
 */

// ==========================================
// BACKEND RESPONSE WRAPPER
// ==========================================

@Keep
data class GetResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any? = null
) {
    fun isSuccess(): Boolean = status == 200
}

// ==========================================
// BARKOD RESPONSE
// ==========================================

@Keep
data class BarkodResponse(
    @SerializedName("etiketler")
    val etiketler: List<BarkodEtiket>,

    @SerializedName("htmlContent")
    val htmlContent: String? = null,

    @SerializedName("toplamEtiket")
    val toplamEtiket: Int
) {
    @Keep
    data class BarkodEtiket(
        @SerializedName("urunSeriNo")
        val urunSeriNo: String  // R00000000001 formatÄ±nda
    )

    // RAF seri numaralarÄ±nÄ± al
    fun getRafNumbers(): List<String> = etiketler.map { it.urunSeriNo }
}

// ==========================================
// UI STATE
// ==========================================

@Keep
data class RafEtiketUiState(
    val isLoading: Boolean = false,
    val adet: String = "",  // TextField iÃ§in String
    val rafNumbers: List<String> = emptyList(),
    val errorMessage: String? = null,
    val isPrinting: Boolean = false,
    val printedCount: Int = 0,
    val totalCount: Int = 0
) {
    // Adet geÃ§erli mi?
    fun isValidAdet(): Boolean {
        val adetInt = adet.toIntOrNull() ?: return false
        return adetInt in 1..1000
    }

    // Print hazÄ±r mÄ±?
    fun isReadyToPrint(): Boolean = rafNumbers.isNotEmpty() && !isPrinting

    // Progress yÃ¼zdesi
    fun getPrintProgress(): Float = if (totalCount > 0) printedCount.toFloat() / totalCount else 0f
}

// ==========================================
// REPOSITORY RESULT
// ==========================================

sealed class RafEtiketResult<out T> {
    object Loading : RafEtiketResult<Nothing>()
    data class Success<T>(val data: T) : RafEtiketResult<T>()
    data class Error(val message: String) : RafEtiketResult<Nothing>()
}