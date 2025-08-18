package com.example.stokkontrolveyonetimsistemi.data.model.urun

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Ürün Create Response
 */
@Keep
data class UrunResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("urunSeriNo")
    val urunSeriNo: String,

    @SerializedName("message")
    val message: String? = null
)