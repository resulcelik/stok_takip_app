package com.example.stokkontrolveyonetimsistemi.data.model.urun

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Ürün Bilgileri - Mobile kayıt için
 * Complete registration içinde kullanılacak
 */
@Keep
data class UrunBilgileri(
    @SerializedName("tasnifNo")
    val tasnifNo: String,

    @SerializedName("aciklama")
    val aciklama: String,

    @SerializedName("markaId")
    val markaId: Long? = null,  // Marka seçilmeyebilir, default null

    @SerializedName("stokBirimiId")
    val stokBirimiId: Long,

    @SerializedName("stokBirimi2Id")
    val stokBirimi2Id: Long? = null,

    @SerializedName("en")
    val en: Double? = null,

    @SerializedName("boy")
    val boy: Double? = null,

    @SerializedName("yukseklik")
    val yukseklik: Double? = null
) {
    /**
     * Validation - Zorunlu alanlar kontrolü
     */
    fun isValid(): Boolean {
        return tasnifNo.isNotBlank() &&
                aciklama.isNotBlank() &&
                stokBirimiId > 0
    }

    /**
     * Boyut bilgisi var mı?
     */
    fun hasDimensions(): Boolean {
        return en != null || boy != null || yukseklik != null
    }
}