// data/model/registration/MobileRegistrationResponse.kt
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

/**
 * Registration Step - Workflow takibi için
 */
enum class RegistrationStep(val stepNumber: Int, val title: String) {
    LOCATION_CHECK(1, "Lokasyon Kontrolü"),
    RAF_SCAN(2, "RAF Etiketi Okutma"),
    URUN_DETAIL(3, "Ürün Bilgileri"),
    PHOTO_CAPTURE(4, "Fotoğraf Çekimi"),
    REVIEW_SUBMIT(5, "Kontrol ve Kayıt");

    fun getNext(): RegistrationStep? {
        return values().find { it.stepNumber == this.stepNumber + 1 }
    }

    fun getPrevious(): RegistrationStep? {
        return values().find { it.stepNumber == this.stepNumber - 1 }
    }

    fun isFirstStep(): Boolean = this == LOCATION_CHECK
    fun isLastStep(): Boolean = this == REVIEW_SUBMIT
}