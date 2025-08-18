package com.example.stokkontrolveyonetimsistemi.data.model.raf

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * RAF Create Request - Mobile terminal için
 * POST /api/user/main/raf/create-mobile
 */
@Keep
data class RafCreateRequest(
    @SerializedName("rafSeriNo")
    val rafSeriNo: String,

    @SerializedName("depoId")
    val depoId: Long,

    @SerializedName("aciklama")
    val aciklama: String = "Terminal RAF kaydı"
)