package com.example.stokkontrolveyonetimsistemi.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Generic Get Response from Backend
 * Used for operations that return data
 */
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

    fun hasData(): Boolean = data != null
}