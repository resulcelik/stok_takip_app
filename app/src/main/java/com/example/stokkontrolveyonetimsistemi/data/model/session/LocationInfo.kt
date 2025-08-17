package com.example.stokkontrolveyonetimsistemi.data.model.session

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LocationInfo(
    @SerializedName("bolgeId")
    val bolgeId: Long,

    @SerializedName("bolgeAdi")
    val bolgeAdi: String,

    @SerializedName("depoId")
    val depoId: Long,

    @SerializedName("depoAdi")
    val depoAdi: String
)