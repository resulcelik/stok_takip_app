package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.location.LocationResult
import com.example.stokkontrolveyonetimsistemi.data.model.location.StokBirimiDto
import com.example.stokkontrolveyonetimsistemi.data.model.raf.RafCreateRequest
import com.example.stokkontrolveyonetimsistemi.data.model.raf.RafCreateResponse
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserSessionDto
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunRequest
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunResponse
import com.example.stokkontrolveyonetimsistemi.data.network.api.MobileApiService
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.UrunCreateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Mobile Registration Repository - BACKEND UYUMLU
 * Backend controller'ları ile uyumlu çalışacak şekilde düzenlendi
 */
class MobileRegistrationRepository(
    private val mobileApiService: MobileApiService,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "MobileRegistrationRepo"
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MIN_PHOTO_COUNT = 4
        private const val RAF_SERIAL_PATTERN = "^R\\d{11}$"
        private const val URUN_SERIAL_PATTERN = "^U\\d{11}$"
    }

    // ==========================================
    // SESSION & LOCATION OPERATIONS
    // ==========================================

    /**
     * Get current user session with location info
     */
    suspend fun getCurrentSession(): Flow<LocationResult<UserSessionDto>> {
        return userRepository.getCurrentUserSession()
    }

    /**
     * Get selected depot ID from session
     */
    suspend fun getSelectedDepoId(): Long? {
        return try {
            var depoId: Long? = null

            getCurrentSession().collect { result ->
                if (result is LocationResult.Success) {
                    depoId = result.data.selectedDepoId
                }
            }

            depoId
        } catch (e: Exception) {
            Log.e(TAG, "Error getting depo ID", e)
            null
        }
    }

    // ==========================================
    // RAF OPERATIONS
    // ==========================================

    /**
     * Create or validate RAF
     * Backend: POST /api/user/main/raf/create-mobile
     */
    suspend fun createMobileRaf(rafSeriNo: String): Flow<MobileResult<RafCreateResponse>> = flow {
        try {
            emit(MobileResult.Loading)

            // Token kontrolü
            if (!tokenStorage.isTokenValid()) {
                emit(MobileResult.TokenExpired)
                return@flow
            }

            // RAF format validasyonu
            if (!isValidRafFormat(rafSeriNo)) {
                emit(MobileResult.ValidationError("RAF numarası formatı hatalı. R ile başlamalı ve 12 karakter olmalı."))
                return@flow
            }

            // Depo ID al
            val depoId = getSelectedDepoId()
            if (depoId == null || depoId <= 0) {
                emit(MobileResult.ValidationError("Lokasyon seçimi yapılmamış. Lütfen önce depo seçiniz."))
                return@flow
            }

            val request = RafCreateRequest(
                rafSeriNo = rafSeriNo,
                depoId = depoId,
                aciklama = "Mobile Terminal RAF kaydı"
            )

            Log.d(TAG, "Creating RAF: $rafSeriNo for depoId: $depoId")

            val response = mobileApiService.createMobileRaf(request)

            when {
                response.isSuccessful -> {
                    // Backend sadece MessageResponse döndürüyor, data yok
                    // Success durumunda basit bir response oluştur
                    Log.d(TAG, "RAF created successfully: $rafSeriNo")
                    emit(MobileResult.Success(
                        RafCreateResponse(
                            rafId = 0, // Backend'den ID gelmiyor
                            rafSeriNo = rafSeriNo
                        )
                    ))
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(MobileResult.TokenExpired)
                }

                response.code() == 409 -> { // Conflict - RAF zaten var
                    emit(MobileResult.Error("Bu RAF numarası zaten kayıtlı"))
                }

                else -> {
                    val errorBody = response.errorBody()?.string()
                    emit(MobileResult.Error("RAF oluşturulamadı: ${errorBody ?: response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating RAF", e)
            emit(MobileResult.Error(handleNetworkError(e)))
        }
    }

    private fun isValidRafFormat(rafSeriNo: String): Boolean {
        return rafSeriNo.matches(Regex(RAF_SERIAL_PATTERN))
    }

    // ==========================================
    // ÜRÜN OPERATIONS
    // ==========================================

    /**
     * Create Ürün
     * Backend: POST /api/user/main/urun/create
     */
    suspend fun createUrun(request: UrunCreateRequest): Flow<MobileResult<UrunResponse>> = flow {
        try {
            emit(MobileResult.Loading)

            if (!tokenStorage.isTokenValid()) {
                emit(MobileResult.TokenExpired)
                return@flow
            }

            // Ürün format validasyonu
            if (!isValidUrunFormat(request.urunSeriNo)) {
                emit(MobileResult.ValidationError("Ürün numarası formatı hatalı. U ile başlamalı ve 12 karakter olmalı."))
                return@flow
            }

            // Backend'e uygun request oluştur
            val urunRequest = UrunRequest(
                urunSeriNo = request.urunSeriNo,
                rafSeriNo = request.rafSeriNo,
                aciklama = request.aciklama,
                bolgeId = request.bolgeId,        // 🔴 EKLE
                depoId = request.depoId,          // 🔴 EKLE
                rafId = request.rafId,
                markaId = null,
                stokBirimiId = request.stokBirimiId,
                stokBirimi2Id = request.stokBirimi2Id,
                en = request.en,
                boy = request.boy,
                yukseklik = request.yukseklik,
            )

            Log.d(TAG, "Creating Ürün: ${request.urunSeriNo} on RAF: ${request.rafSeriNo}")

            val response = mobileApiService.createUrun(urunRequest)

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null && body.status == 200) {
                        // Backend MessageResponse döndürüyor, UrunResponse'a çevir
                        val urunResponse = UrunResponse(
                            id = body.id,
                            urunSeriNo = request.urunSeriNo,
                            message = body.message
                        )

                        Log.d(TAG, "Ürün created successfully: ${request.urunSeriNo}")
                        emit(MobileResult.Success(urunResponse))
                    } else {
                        emit(MobileResult.Error(body?.message ?: "Ürün oluşturulamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(MobileResult.TokenExpired)
                }

                response.code() == 400 -> {
                    emit(MobileResult.ValidationError("Hatalı veri. Lütfen bilgileri kontrol ediniz."))
                }

                response.code() == 409 -> {
                    emit(MobileResult.Error("Bu ürün numarası zaten kayıtlı"))
                }

                else -> {
                    val errorBody = response.errorBody()?.string()
                    emit(MobileResult.Error("Ürün oluşturulamadı: ${errorBody ?: response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating Ürün", e)
            emit(MobileResult.Error(handleNetworkError(e)))
        }
    }

    private fun isValidUrunFormat(urunSeriNo: String): Boolean {
        return urunSeriNo.matches(Regex(URUN_SERIAL_PATTERN))
    }

    /**
     * Get stock units for dropdown
     */
    suspend fun getStokBirimleri(): Flow<LocationResult<List<StokBirimiDto>>> {
        return locationRepository.getAllStokBirimi()
    }

    // ==========================================
    // PHOTO OPERATIONS
    // ==========================================

    /**
     * Upload photo for created Ürün
     * Backend: POST /api/user/main/mobile/file/urun/{urunSeriNo}/upload-photo
     */
    suspend fun uploadPhotoForUrun(
        id: Long,
        photoFile: File
    ): Flow<MobileResult<PhotoUploadResponse>> = flow {
        try {
            emit(MobileResult.Loading)

            // Token kontrolü
            if (!tokenStorage.isTokenValid()) {
                emit(MobileResult.TokenExpired)
                return@flow
            }

            // Dosya kontrolü
            if (!photoFile.exists()) {
                emit(MobileResult.Error("Fotoğraf dosyası bulunamadı"))
                return@flow
            }

            if (photoFile.length() > MAX_FILE_SIZE) {
                emit(MobileResult.ValidationError("Fotoğraf boyutu 10MB'dan büyük olamaz"))
                return@flow
            }

            Log.d(TAG, "Uploading photo for Ürün: $id, file: ${photoFile.name}")

            // Multipart body oluştur
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)

            val response = mobileApiService.uploadUrunPhoto(id, photoPart)

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null && body.status == 200) {
                        Log.d(TAG, "Photo uploaded successfully for Ürün: $id")
                        emit(MobileResult.Success(
                            PhotoUploadResponse(
                                fileName = photoFile.name,
                                message = body.message
                            )
                        ))
                    } else {
                        emit(MobileResult.Error(body?.message ?: "Fotoğraf yüklenemedi"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(MobileResult.TokenExpired)
                }

                response.code() == 404 -> {
                    emit(MobileResult.Error("Ürün bulunamadı: $id"))
                }

                response.code() == 413 -> {
                    emit(MobileResult.Error("Fotoğraf boyutu çok büyük"))
                }

                else -> {
                    emit(MobileResult.Error("Fotoğraf yüklenemedi: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo for Ürün: $id", e)
            emit(MobileResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Upload multiple photos for Ürün
     * Backend: POST /api/user/main/mobile/file/urun/{urunSeriNo}/upload-multiple-photos
     */
    suspend fun uploadMultiplePhotosForUrun(
        urunSeriNo: String,
        photoFiles: List<File>
    ): Flow<MobileResult<MultiPhotoUploadResponse>> = flow {
        try {
            emit(MobileResult.Loading)

            if (!tokenStorage.isTokenValid()) {
                emit(MobileResult.TokenExpired)
                return@flow
            }

            if (photoFiles.isEmpty()) {
                emit(MobileResult.ValidationError("Fotoğraf seçilmedi"))
                return@flow
            }

            Log.d(TAG, "Uploading ${photoFiles.size} photos for Ürün: $urunSeriNo")

            // MultipartBody.Part listesi oluştur
            val photoParts = mutableListOf<MultipartBody.Part>()

            photoFiles.forEach { file ->
                if (file.exists() && file.length() <= MAX_FILE_SIZE) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("photos", file.name, requestFile)
                    photoParts.add(part)
                }
            }

            if (photoParts.isEmpty()) {
                emit(MobileResult.ValidationError("Geçerli fotoğraf bulunamadı"))
                return@flow
            }

            val response = mobileApiService.uploadMultipleUrunPhotos(urunSeriNo, photoParts)

            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null && body.status == 200) {
                        Log.d(TAG, "Multiple photos uploaded successfully for Ürün: $urunSeriNo")
                        emit(MobileResult.Success(
                            MultiPhotoUploadResponse(
                                uploadedCount = photoParts.size,
                                message = body.message
                            )
                        ))
                    } else {
                        emit(MobileResult.Error(body?.message ?: "Fotoğraflar yüklenemedi"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(MobileResult.TokenExpired)
                }

                else -> {
                    emit(MobileResult.Error("Fotoğraflar yüklenemedi: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading multiple photos", e)
            emit(MobileResult.Error(handleNetworkError(e)))
        }
    }

    // ==========================================
    // HELPER FUNCTIONS
    // ==========================================

    private fun handleNetworkError(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "İnternet bağlantısı yok"
            is SocketTimeoutException -> "Bağlantı zaman aşımına uğradı"
            else -> e.localizedMessage ?: "Beklenmeyen bir hata oluştu"
        }
    }
}

// ==========================================
// RESULT WRAPPER
// ==========================================

sealed class MobileResult<out T> {
    object Loading : MobileResult<Nothing>()
    data class Success<T>(val data: T) : MobileResult<T>()
    data class Error(val message: String) : MobileResult<Nothing>()
    data class ValidationError(val message: String) : MobileResult<Nothing>()
    object TokenExpired : MobileResult<Nothing>()
}

// ==========================================
// RESPONSE MODELS
// ==========================================

data class PhotoUploadResponse(
    val fileName: String,
    val message: String
)

data class MultiPhotoUploadResponse(
    val uploadedCount: Int,
    val message: String
)