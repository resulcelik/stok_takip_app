package com.example.stokkontrolveyonetimsistemi.response
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Generic Message Response from Backend
 * Used for operations that return status and message
 */
@Keep
data class MessageResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String
) {
    fun isSuccess(): Boolean = status == 200
}