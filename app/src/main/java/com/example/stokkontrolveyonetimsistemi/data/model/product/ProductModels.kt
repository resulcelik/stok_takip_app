package com.example.stokkontrolveyonetimsistemi.data.model.product

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


/**
 * API Response Wrapper
 */
@Keep
data class UrunGetResponse(
    @SerializedName("status")
    val status: Int = 0,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: UrunResponseData? = null
) {
    fun isSuccess(): Boolean = status == 200
}

/**
 * Response Data - Ana veri yapısı
 */
@Keep
data class UrunResponseData(
    @SerializedName("etiketData")
    val etiketData: EtiketData? = null,

    @SerializedName("urunSeriNo")
    val urunSeriNo: String? = null,

    @SerializedName("batchId")
    val batchId: String? = null,

    @SerializedName("adet")
    val adet: Int = 0
)

/**
 * Etiket Data - Etiket detayları
 */
@Keep
data class EtiketData(
    @SerializedName("etiket")
    val etiket: UrunEtiket? = null,

    @SerializedName("etiketler")
    val etiketler: List<UrunEtiket>? = null,

    @SerializedName("toplamEtiketSayisi")
    val toplamEtiketSayisi: Int = 0,

    @SerializedName("etiketBoyutu")
    val etiketBoyutu: String? = null,

    @SerializedName("uretimTarihi")
    val uretimTarihi: String? = null,

    @SerializedName("kullanici")
    val kullanici: String? = null,

    @SerializedName("htmlContent")
    val htmlContent: String? = null,

    @SerializedName("ozet")
    val ozet: String? = null
)

/**
 * Tekil Etiket
 */
@Keep
data class UrunEtiket(
    @SerializedName("urunSeriNo")
    val urunSeriNo: String? = null,

    @SerializedName("barkodData")
    val barkodData: String? = null,

    @SerializedName("tarihSaat")
    val tarihSaat: String? = null,

    @SerializedName("etiketBoyutu")
    val etiketBoyutu: String? = null,

    @SerializedName("formattedDateTime")
    val formattedDateTime: String? = null,

    @SerializedName("genislik")
    val genislik: Int = 0,

    @SerializedName("yukseklik")
    val yukseklik: Int = 0
)

/**
 * ÜRÜN Barkod Response - ViewModel'de kullanılacak basitleştirilmiş model
 */
data class UrunBarkodResponse(
    val urunSeriNo: String,
    val toplamAdet: Int,
    val etiketler: List<UrunEtiket>
) {
    /**
     * Yazdırılacak numara - TEK NUMARA
     */
    fun getPrintNumber(): String = urunSeriNo

    /**
     * Kaç adet yazdırılacak
     */
    fun getPrintCount(): Int = toplamAdet

    /**
     * UI'da gösterilecek numaralar listesi
     */
    fun getUrunNumbers(): List<String> {
        // Aynı numarayı adet kadar göster
        return List(toplamAdet) { urunSeriNo }
    }
}

// ==========================================
// UI STATE MODELS
// ==========================================

/**
 * ÜRÜN Etiket UI State
 */
data class UrunEtiketUiState(
    val isLoading: Boolean = false,
    val lastGeneratedNumbers: List<String> = emptyList(),
    val lastPrintNumber: String? = null,  // Yazdırılan tek numara
    val printCount: Int = 0,  // Kaç adet yazdırılacak
    val totalGenerated: Int = 0,
    val todayGenerated: Int = 0,
    val error: String? = null
) {
    fun isValidAdet(adet: String): Boolean {
        val adetInt = adet.toIntOrNull() ?: return false
        return adetInt in 1..100
    }

    fun isReadyToGenerate(): Boolean = !isLoading && error == null
}

/**
 * Printer State
 */
data class PrinterState(
    val isConnected: Boolean = false,
    val isPrinting: Boolean = false,
    val printerName: String? = null,
    val printedCount: Int = 0,
    val totalCount: Int = 0
) {
    fun getProgressPercentage(): Int {
        return if (totalCount > 0) {
            ((printedCount.toFloat() / totalCount) * 100).toInt()
        } else 0
    }
}

// ==========================================
// EVENTS & RESULTS
// ==========================================

sealed class UrunEtiketEvent {
    data class GenerationSuccess(val number: String, val count: Int) : UrunEtiketEvent()
    data class PrintSuccess(val count: Int) : UrunEtiketEvent()
    data object PrinterConnected : UrunEtiketEvent()
    data object PrinterDisconnected : UrunEtiketEvent()
    data class Error(val message: String) : UrunEtiketEvent()
    data class PrintError(val message: String) : UrunEtiketEvent()
    data class PrintProgress(val current: Int, val total: Int) : UrunEtiketEvent()
}

/**
 * Repository Result Wrapper
 */
sealed class UrunEtiketResult<out T> {
    data object Loading : UrunEtiketResult<Nothing>()
    data class Success<T>(val data: T) : UrunEtiketResult<T>()
    data class Error(val message: String) : UrunEtiketResult<Nothing>()
}