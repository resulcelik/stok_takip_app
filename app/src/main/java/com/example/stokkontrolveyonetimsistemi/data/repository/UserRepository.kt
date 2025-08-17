// data/repository/UserRepository.kt

package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.auth.MessageResponse
import com.example.stokkontrolveyonetimsistemi.data.model.location.LocationResult
import com.example.stokkontrolveyonetimsistemi.data.model.location.SetLocationRequest
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserSessionDto
import com.example.stokkontrolveyonetimsistemi.data.network.api.UserApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * User Repository
 * Kullanıcı session ve profil yönetimi
 */
class UserRepository(
    private val userApiService: UserApiService,
    private val tokenStorage: TokenStorage
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    /**
     * Get current user session
     */
    suspend fun getCurrentUserSession(): Flow<LocationResult<UserSessionDto>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Fetching current user session")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = userApiService.getCurrentSession()

            when {
                response.isSuccessful -> {
                    val getResponse = response.body()

                    if (getResponse != null && getResponse.isSuccess()) {
                        val userSession = getResponse.getDataOrNull()
                        if (userSession != null) {
                            Log.d(TAG, "User session loaded: ${userSession.username}")

                            // Save username to storage
                            tokenStorage.saveUserInfo(userSession.username)

                            emit(LocationResult.Success(userSession))
                        } else {
                            emit(LocationResult.Error("Kullanıcı bilgileri alınamadı"))
                        }
                    } else {
                        emit(LocationResult.Error(getResponse?.message ?: "Kullanıcı bilgileri alınamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("Kullanıcı bilgileri alınamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user session", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Set user location
     */
    suspend fun setUserLocation(bolgeId: Long, depoId: Long): Flow<LocationResult<String>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Setting user location: bolgeId=$bolgeId, depoId=$depoId")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val request = SetLocationRequest(
                bolgeId = bolgeId,
                depoId = depoId
            )

            val response = userApiService.setUserLocation(request)

            when {
                response.isSuccessful -> {
                    val messageResponse = response.body()

                    if (messageResponse != null && messageResponse.isSuccess()) {
                        Log.d(TAG, "Location set successfully: ${messageResponse.message}")
                        emit(LocationResult.Success(messageResponse.message))
                    } else {
                        emit(LocationResult.Error(messageResponse?.message ?: "Lokasyon ayarlanamadı"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("Lokasyon ayarlanamadı: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting user location", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Clear user location
     */
    suspend fun clearUserLocation(): Flow<LocationResult<String>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Clearing user location")

            if (!tokenStorage.isTokenValid()) {
                emit(LocationResult.TokenExpired)
                return@flow
            }

            val response = userApiService.clearUserLocation()

            when {
                response.isSuccessful -> {
                    val messageResponse = response.body()

                    if (messageResponse != null && messageResponse.isSuccess()) {
                        Log.d(TAG, "Location cleared successfully")
                        emit(LocationResult.Success(messageResponse.message))
                    } else {
                        emit(LocationResult.Error(messageResponse?.message ?: "Lokasyon temizlenemedi"))
                    }
                }

                response.code() == ApiConstants.HTTP_UNAUTHORIZED -> {
                    tokenStorage.clearExpiredToken()
                    emit(LocationResult.TokenExpired)
                }

                else -> {
                    emit(LocationResult.Error("Lokasyon temizlenemedi: ${response.message()}"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user location", e)
            emit(LocationResult.Error(handleNetworkError(e)))
        }
    }

    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean = tokenStorage.isTokenValid()

    /**
     * Handle network errors
     */
    private fun handleNetworkError(e: Exception): String {
        return when (e) {
            is SocketTimeoutException -> "Bağlantı zaman aşımına uğradı"
            is UnknownHostException -> "Sunucuya bağlanılamıyor"
            is SSLException -> "Güvenli bağlantı kurulamadı"
            else -> "Ağ hatası: ${e.localizedMessage}"
        }
    }
}

/**
 * MessageResponse extension to check success
 */
fun MessageResponse.isSuccess(): Boolean {
    return this.status in 200..299
}