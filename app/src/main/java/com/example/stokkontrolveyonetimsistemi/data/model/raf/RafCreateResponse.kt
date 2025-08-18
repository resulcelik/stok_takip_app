package com.example.stokkontrolveyonetimsistemi.data.model.raf

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * RAF Create Response
 * Backend'den dönen RAF kayıt sonucu
 */
@Keep
data class RafCreateResponse(
    @SerializedName("rafId")
    val rafId: Long,

    @SerializedName("rafSeriNo")
    val rafSeriNo: String
)