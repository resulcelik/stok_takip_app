package com.example.stokkontrolveyonetimsistemi.data.model.common

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Generic Mobile API Response Wrapper
 * Tüm mobile endpoint'ler için ortak response yapısı
 */
@Keep
data class MobileResponse<T>(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null
) {
    fun isSuccess(): Boolean = status == 200

    fun getDataOrNull(): T? = if (isSuccess()) data else null

    fun getErrorMessage(): String = if (!isSuccess()) message else ""
}