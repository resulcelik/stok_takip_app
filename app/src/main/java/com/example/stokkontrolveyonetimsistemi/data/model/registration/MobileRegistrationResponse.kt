package com.example.stokkontrolveyonetimsistemi.data.model.registration

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Mobile Complete Registration Response
 * Başarılı kayıt sonucu dönen response
 */
@Keep
data class MobileRegistrationResponse(
    @SerializedName("rafId")
    val rafId: Long,

    @SerializedName("urunId")
    val urunId: Long,

    @SerializedName("urunSeriNo")
    val urunSeriNo: String,

    @SerializedName("uploadedPhotos")
    val uploadedPhotos: Int,

    @SerializedName("registrationTime")
    val registrationTime: String? = null
)

/**
 * Registration State - ViewModel için
 * Kayıt sürecinin durumunu takip etmek için
 */
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val response: MobileRegistrationResponse) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}
