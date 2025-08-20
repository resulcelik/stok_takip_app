package com.example.stokkontrolveyonetimsistemi.data.model.urun

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Ürün Numarası Preview Response
 * GET /api/user/main/urun/preview-next
 */
@Keep
data class UrunPreviewResponse(
    @SerializedName("sonrakiNumara")
    val sonrakiNumara: String,

    @SerializedName("durum")
    val durum: String = "ÖNİZLEME"
)