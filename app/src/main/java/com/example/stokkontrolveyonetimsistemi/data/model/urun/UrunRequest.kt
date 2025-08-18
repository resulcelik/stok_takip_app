package com.example.stokkontrolveyonetimsistemi.data.model.urun

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Ürün Create Request - Backend uyumlu
 * POST /api/user/main/urun/create
 */
@Keep
data class UrunRequest(
    @SerializedName("urunSeriNo")
    val urunSeriNo: String,

    @SerializedName("rafSeriNo")
    val rafSeriNo: String,

    @SerializedName("aciklama")
    val aciklama: String,

    @SerializedName("markaId")
    val markaId: Long? = null,

    @SerializedName("stokBirimiId")
    val stokBirimiId: Long,

    @SerializedName("stokBirimi2Id")
    val stokBirimi2Id: Long? = null,

    @SerializedName("en")
    val en: Double? = null,

    @SerializedName("boy")
    val boy: Double? = null,

    @SerializedName("yukseklik")
    val yukseklik: Double? = null,

    @SerializedName("bolgeId")
    val bolgeId: Long,  // EKLE

    @SerializedName("depoId")
    val depoId: Long,   // EKLE

    @SerializedName("rafId")
    val rafId: Long?,   // EKLE (RAF'tan dönüyorsa)

) {
    /**
     * Request temizleme
     */
    fun sanitize(): UrunRequest {
        return this.copy(
            urunSeriNo = urunSeriNo.trim().uppercase(),
            rafSeriNo = rafSeriNo.trim().uppercase(),
            aciklama = aciklama.trim()
        )
    }

    /**
     * Zorunlu alanlar kontrolü
     */
    fun hasRequiredFields(): Boolean {
        return urunSeriNo.isNotBlank() &&
                rafSeriNo.isNotBlank() &&
                aciklama.isNotBlank() &&
                stokBirimiId > 0
    }

    /**
     * Ürün seri no format kontrolü
     */
    fun isValidUrunSeriNo(): Boolean {
        return urunSeriNo.startsWith("U") && urunSeriNo.length == 12
    }

    /**
     * RAF seri no format kontrolü
     */
    fun isValidRafSeriNo(): Boolean {
        return rafSeriNo.startsWith("R") && rafSeriNo.length == 12
    }
}
