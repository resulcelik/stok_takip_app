package com.example.stokkontrolveyonetimsistemi.data.model.session

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LocationValidationDto(
    @SerializedName("hasLocation")
    val hasLocation: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("location")
    val location: LocationInfo?
)