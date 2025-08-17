package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.network.api.LocationApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Location repository
 * Hiyerarşik lokasyon yönetimi ve cascade dropdown operations
 *
 * ✅ UPDATED: setUserLocation metodu KALDIRILDI (UserRepository'de mevcut)
 *
 * Dosya Konumu: /data/repository/LocationRepository.kt
 * Pattern: Same as AuthRepository.kt (Flow-based, error handling)
 */
class LocationRepository(
    private val locationApiService: LocationApiService,
    private val tokenStorage: TokenStorage
) {

    companion object {
        private const val TAG = "LocationRepository"
    }

    // ==========================================
    // HIERARCHICAL LOCATION OPERATIONS
    // ==========================================

    /**
     * Get all regions (Bölgeler)
     * First level of cascade dropdown
     */
    suspend fun getAllBolge(): Flow<LocationResult<List<BolgeDto>>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching all regions")

            // Check token first
            if (!tokenStorage.isTokenValid()) {
                Log.w(TAG, "Token invalid - cannot get regions")
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = locationApiService.getAllBolge()

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val regions = getResponse.getDataOrNull() ?: emptyList()
                        Log.d(TAG, "Regions loaded successfully: ${regions.size} items")
                        emit(LocationResult.Success(regions))
                    } else {
                        Log.e(TAG, "Invalid regions response: ${getResponse?.message}")
                        emit(LocationResult.Error("Bölge listesi alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    Log.w(TAG, "Unauthorized - token expired")
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    Log.e(TAG, "Get regions failed: ${response.code()}")
                    emit(LocationResult.Error("Bölge listesi alınamadı: ${response.message()}"))
                }
            }

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Regions fetch timeout", e)
            emit(LocationResult.Error("Bağlantı zaman aşımı"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching regions", e)
            emit(LocationResult.Error("İnternet bağlantısı bulunamadı"))
        } catch (e: HttpException) {
            handleHttpException(e)?.let { emit(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching regions", e)
            emit(LocationResult.Error("Beklenmeyen hata: ${e.message}"))
        }
    }

    /**
     * Get provinces by region (İller by Bölge)
     * Second level of cascade dropdown
     */
    suspend fun getIllerByBolge(bolgeId: Long): Flow<LocationResult<List<IlDto>>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching provinces for region: $bolgeId")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = locationApiService.getIllerByBolge(bolgeId)

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val provinces = getResponse.getDataOrNull() ?: emptyList()
                        Log.d(TAG, "Provinces loaded: ${provinces.size} items for region $bolgeId")
                        emit(LocationResult.Success(provinces))
                    } else {
                        emit(LocationResult.Error("İl listesi alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("İl listesi alınamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching provinces", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Get districts by province (İlçeler by İl)
     * Third level of cascade dropdown
     */
    suspend fun getIlcelerByIl(ilId: Long): Flow<LocationResult<List<IlceDto>>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching districts for province: $ilId")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = locationApiService.getIlcelerByIl(ilId)

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val districts = getResponse.getDataOrNull() ?: emptyList()
                        Log.d(TAG, "Districts loaded: ${districts.size} items for province $ilId")
                        emit(LocationResult.Success(districts))
                    } else {
                        emit(LocationResult.Error("İlçe listesi alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("İlçe listesi alınamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching districts", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Get warehouses by district (Depolar by İlçe)
     * Fourth level of cascade dropdown
     */
    suspend fun getDepolarByIlce(ilceId: Long): Flow<LocationResult<List<DepoDto>>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching warehouses for district: $ilceId")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = locationApiService.getDepolarByIlce(ilceId)

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val warehouses = getResponse.getDataOrNull() ?: emptyList()
                        // Filter only active warehouses
                        val activeWarehouses = warehouses.filter { it.isAvailable() }
                        Log.d(TAG, "Active warehouses loaded: ${activeWarehouses.size} items for district $ilceId")
                        emit(LocationResult.Success(activeWarehouses))
                    } else {
                        emit(LocationResult.Error("Depo listesi alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("Depo listesi alınamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching warehouses", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    // ==========================================
    // ✅ REMOVED: SESSION LOCATION MANAGEMENT
    // ==========================================
    // setUserLocation metodu KALDIRILDI
    // Bu işlem artık UserRepository'de yapılıyor
    // UserApiService kullanılarak /api/user/session/set-location endpoint'i çağrılıyor

    // ==========================================
    // UTILITY OPERATIONS
    // ==========================================

    /**
     * Get all stock units (Stok Birimleri)
     * For product creation form
     */
    suspend fun getAllStokBirimi(): Flow<LocationResult<List<StokBirimiDto>>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching stock units")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = locationApiService.getAllStokBirimi()

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val stockUnits = getResponse.getDataOrNull() ?: emptyList()
                        Log.d(TAG, "Stock units loaded: ${stockUnits.size} items")
                        emit(LocationResult.Success(stockUnits))
                    } else {
                        emit(LocationResult.Error("Stok birimi listesi alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("Stok birimi listesi alınamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stock units", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    // ==========================================
    // VALIDATION OPERATIONS
    // ==========================================

    /**
     * Validate location selection
     * Business logic validation before setting location
     */
    fun validateLocationSelection(
        selectedBolge: BolgeDto?,
        selectedDepo: DepoDto?
    ): LocationResult<Boolean> {
        return when {
            selectedBolge == null -> LocationResult.Error("Bölge seçimi gerekli")
            selectedDepo == null -> LocationResult.Error("Depo seçimi gerekli")
            !selectedDepo.isAvailable() -> LocationResult.Error("Seçilen depo aktif değil")
            else -> LocationResult.Success(true)
        }
    }

    // ==========================================
    // TOKEN MANAGEMENT UTILITIES
    // ==========================================

    /**
     * Check if user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return tokenStorage.isTokenValid()
    }

    // ==========================================
    // ERROR HANDLING UTILITIES
    // ==========================================

    /**
     * Handle HTTP exceptions with proper user messages
     */
    private fun handleHttpException(exception: HttpException): LocationResult<Nothing>? {
        return when (exception.code()) {
            ApiConstants.HTTP_UNAUTHORIZED -> {
                tokenStorage.clearExpiredToken()
                LocationResult.TokenExpired
            }
            ApiConstants.HTTP_FORBIDDEN -> LocationResult.Error("Bu işlem için yetkiniz bulunmuyor")
            ApiConstants.HTTP_NOT_FOUND -> LocationResult.Error("İstenen kaynak bulunamadı")
            ApiConstants.HTTP_INTERNAL_SERVER_ERROR -> LocationResult.Error("Sunucu hatası")
            else -> LocationResult.Error("İşlem başarısız: ${exception.message()}")
        }
    }

    /**
     * Handle network errors with user-friendly messages
     */
    private fun handleNetworkError(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> "Bağlantı zaman aşımı"
            is IOException -> "İnternet bağlantısı bulunamadı"
            is HttpException -> "Sunucu hatası (${exception.code()})"
            else -> "Beklenmeyen hata oluştu: ${exception.message}"
        }
    }
}