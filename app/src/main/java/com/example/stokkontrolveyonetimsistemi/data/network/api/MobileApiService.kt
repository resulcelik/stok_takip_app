package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.raf.RafCreateRequest
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunRequest
import com.example.stokkontrolveyonetimsistemi.response.CreateResponse
import com.example.stokkontrolveyonetimsistemi.response.MessageResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Mobile Terminal Operations API Service - BACKEND UYUMLU
 * Backend controller'ları ile uyumlu endpoint'ler
 */
interface MobileApiService {

    // ==========================================
    // RAF İŞLEMLERİ
    // ==========================================

    /**
     * Create or validate RAF for mobile
     * Backend: RafUserController.createMobileRaf()
     * POST /api/user/main/raf/create-mobile
     *
     * @param request RAF bilgileri (rafSeriNo, depoId, aciklama)
     * @return MessageResponse (status, message)
     */
    @POST(ApiConstants.RAF_CREATE_MOBILE)
    suspend fun createMobileRaf(
        @Body request: RafCreateRequest
    ): Response<MessageResponse>

    // ==========================================
    // ÜRÜN İŞLEMLERİ
    // ==========================================

    /**
     * Create Ürün from terminal
     * Backend: UrunUserController.createUrunFromTerminal()
     * POST /api/user/main/urun/create
     *
     * @param request Ürün bilgileri
     * @return MessageResponse (status, message)
     */
    @POST("api/user/main/urun/create")
    suspend fun createUrun(
        @Body request: UrunRequest
    ): Response<CreateResponse>

    /**
     * Update Ürün from terminal
     * Backend: UrunUserController.updateUrunFromTerminal()
     * PUT /api/user/main/urun/{id}
     *
     * @param id Ürün ID
     * @param request Ürün bilgileri
     * @return MessageResponse
     */
    @PUT("api/user/main/urun/{id}")
    suspend fun updateUrun(
        @Path("id") id: Long,
        @Body request: UrunRequest
    ): Response<MessageResponse>

    /**
     * Get Ürün by serial number
     * Backend: UrunUserController.getUrunBySeriNo()
     * GET /api/user/main/urun/by-seri/{seriNo}
     *
     * @param seriNo Ürün seri numarası
     * @return GetResponse with Ürün data
     */
    @GET("api/user/main/urun/by-seri/{seriNo}")
    suspend fun getUrunBySeriNo(
        @Path("seriNo") seriNo: String
    ): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    // ==========================================
    // FOTOĞRAF İŞLEMLERİ
    // ==========================================

    /**
     * Upload single photo for Ürün
     * Backend: MobileFileController.uploadUrunPhoto()
     * POST /api/user/main/mobile/file/urun/{urunSeriNo}/upload-photo
     *
     * @param urunSeriNo Ürün seri numarası
     * @param photo Fotoğraf dosyası
     * @return MessageResponse
     */
    @Multipart
    @POST("api/user/main/mobile/file/urun/{id}/upload-photo")
    suspend fun uploadUrunPhoto(
        @Path("id") id: Long,
        @Part photo: MultipartBody.Part
    ): Response<MessageResponse>

    /**
     * Upload multiple photos for Ürün
     * Backend: MobileFileController.uploadMultipleUrunPhotos()
     * POST /api/user/main/mobile/file/urun/{urunSeriNo}/upload-multiple-photos
     *
     * @param urunSeriNo Ürün seri numarası
     * @param photos Fotoğraf dosyaları
     * @return MessageResponse
     */
    @Multipart
    @POST("api/user/main/mobile/file/urun/{urunSeriNo}/upload-multiple-photos")
    suspend fun uploadMultipleUrunPhotos(
        @Path("urunSeriNo") urunSeriNo: String,
        @Part photos: List<MultipartBody.Part>
    ): Response<MessageResponse>

    /**
     * Upload Base64 photo for Ürün
     * Backend: MobileFileController.uploadBase64Photo()
     * POST /api/user/main/mobile/file/urun/{urunSeriNo}/upload-base64
     *
     * @param urunSeriNo Ürün seri numarası
     * @param request Base64 photo data
     * @return MessageResponse
     */
    @POST("api/user/main/mobile/file/urun/{urunSeriNo}/upload-base64")
    suspend fun uploadBase64Photo(
        @Path("urunSeriNo") urunSeriNo: String,
        @Body request: MobilePhotoRequest
    ): Response<MessageResponse>

    /**
     * Get Ürün photos
     * Backend: MobileFileController.getUrunPhotos()
     * GET /api/user/main/mobile/file/urun/{urunSeriNo}/photos
     *
     * @param urunSeriNo Ürün seri numarası
     * @return GetResponse with photo list
     */
    @GET("api/user/main/mobile/file/urun/{urunSeriNo}/photos")
    suspend fun getUrunPhotos(
        @Path("urunSeriNo") urunSeriNo: String
    ): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Delete photo
     * Backend: MobileFileController.deleteMobilePhoto()
     * DELETE /api/user/main/mobile/file/photo/{gorselUuid}
     *
     * @param gorselUuid Görsel UUID
     * @return MessageResponse
     */
    @DELETE("api/user/main/mobile/file/photo/{gorselUuid}")
    suspend fun deletePhoto(
        @Path("gorselUuid") gorselUuid: String
    ): Response<MessageResponse>

    /**
     * Get photo info
     * Backend: MobileFileController.getMobilePhotoInfo()
     * GET /api/user/main/mobile/file/photo/{gorselUuid}/info
     *
     * @param gorselUuid Görsel UUID
     * @return GetResponse with photo info
     */
    @GET("api/user/main/mobile/file/photo/{gorselUuid}/info")
    suspend fun getPhotoInfo(
        @Path("gorselUuid") gorselUuid: String
    ): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Get user photo statistics
     * Backend: MobileFileController.getMobilePhotoStats()
     * GET /api/user/main/mobile/file/photo-stats
     *
     * @return GetResponse with statistics
     */
    @GET("api/user/main/mobile/file/photo-stats")
    suspend fun getPhotoStats(): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    // ==========================================
    // ÜRÜN ETİKET İŞLEMLERİ
    // ==========================================

    /**
     * Generate Ürün labels (not used in mobile flow anymore)
     * Backend: UrunUserController.generateUrunLabels()
     * POST /api/user/main/urun/generate-labels?adet=30
     *
     * @param adet Etiket adedi
     * @return GetResponse with label data
     */
    @POST("api/user/main/urun/generate-labels")
    suspend fun generateUrunLabels(
        @Query("adet") adet: Int
    ): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Preview next Ürün number (not used anymore - using barcode)
     * Backend: UrunUserController.previewNextNumber()
     * GET /api/user/main/urun/preview-next
     *
     * @return GetResponse with next number
     */
    @GET("api/user/main/urun/preview-next")
    suspend fun previewNextUrunNo(): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Get system status
     * Backend: UrunUserController.getSystemStatus()
     * GET /api/user/main/urun/status
     *
     * @return GetResponse with system status
     */
    @GET("api/user/main/urun/status")
    suspend fun getSystemStatus(): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Get active Ürün list
     * Backend: UrunUserController.getAktifUrunler()
     * GET /api/user/main/urun/list/aktif
     *
     * @return GetResponse with Ürün list
     */
    @GET("api/user/main/urun/list/aktif")
    suspend fun getAktifUrunler(): Response<com.example.stokkontrolveyonetimsistemi.response.GetResponse>

    /**
     * Toggle Ürün active status
     * Backend: UrunUserController.toggleAktifDurum()
     * PATCH /api/user/main/urun/{id}/toggle-aktif
     *
     * @param id Ürün ID
     * @return MessageResponse
     */
    @PATCH("api/user/main/urun/{id}/toggle-aktif")
    suspend fun toggleUrunAktif(
        @Path("id") id: Long
    ): Response<MessageResponse>
}

// ==========================================
// REQUEST/RESPONSE MODELS
// ==========================================

/**
 * Mobile Photo Request for Base64 upload
 */
data class MobilePhotoRequest(
    @SerializedName("photoBase64")
    val photoBase64: String,

    @SerializedName("compressionLevel")
    val compressionLevel: String = "MEDIUM" // LOW, MEDIUM, HIGH
) {
    fun sanitize() {
        // Temizleme işlemleri
    }

    fun isValid(): Boolean {
        return photoBase64.isNotBlank() &&
                (photoBase64.startsWith("data:image/") || photoBase64.length > 100)
    }
}

/**
 * Backend Response Models
 */
data class MessageResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String
)

data class GetResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any? = null
)