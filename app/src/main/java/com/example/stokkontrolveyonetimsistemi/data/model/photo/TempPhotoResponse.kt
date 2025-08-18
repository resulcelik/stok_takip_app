package com.example.stokkontrolveyonetimsistemi.data.model.photo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Geçici Fotoğraf Upload Response
 * POST /api/user/main/mobile/photo/temp-upload
 */
@Keep
data class TempPhotoResponse(
    @SerializedName("tempPhotoId")
    val tempPhotoId: String,

    @SerializedName("index")
    val index: Int,

    @SerializedName("remainingRequired")
    val remainingRequired: Int,

    @SerializedName("uploadedAt")
    val uploadedAt: String? = null
)

/**
 * Local fotoğraf tracking için model
 * UI'da gösterim ve state yönetimi için
 */
@Keep
data class TempPhotoLocal(
    val tempPhotoId: String,
    val localPath: String,
    val index: Int,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val uploadProgress: Int = 0
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    FAILED
}