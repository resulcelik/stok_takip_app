// data/network/api/UserApiService.kt

package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.GetResponse
import com.example.stokkontrolveyonetimsistemi.data.model.auth.MessageResponse
import com.example.stokkontrolveyonetimsistemi.data.model.location.SetLocationRequest
import com.example.stokkontrolveyonetimsistemi.data.model.session.LocationValidationDto
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserProfileDto
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserSessionDto
import retrofit2.Response
import retrofit2.http.*

/**
 * User Session Management API Service
 * Kullanıcı session ve lokasyon yönetimi endpoint'leri
 */
interface UserApiService {

    /**
     * Get current user session
     * GET /api/user/session/current
     *
     * Login sonrası veya dashboard'da kullanıcı bilgilerini getirmek için
     */
    @GET(ApiConstants.SESSION_CURRENT)
    suspend fun getCurrentSession(): Response<GetResponse<UserSessionDto>>

    /**
     * Get user profile
     * GET /api/user/session/profile
     *
     * Kullanıcı profil bilgilerini getirir (şifre hariç)
     */
    @GET(ApiConstants.SESSION_PROFILE)
    suspend fun getUserProfile(): Response<GetResponse<UserProfileDto>>

    /**
     * Set user location
     * POST /api/user/session/set-location
     *
     * Kullanıcının bölge ve depo lokasyonunu ayarlar
     */
    @POST(ApiConstants.SESSION_SET_LOCATION)
    suspend fun setUserLocation(
        @Body request: SetLocationRequest
    ): Response<MessageResponse>

    /**
     * Clear user location
     * DELETE /api/user/session/clear-location
     *
     * Kullanıcının lokasyon bilgilerini temizler
     */
    @DELETE(ApiConstants.SESSION_CLEAR_LOCATION)
    suspend fun clearUserLocation(): Response<MessageResponse>

    /**
     * Validate user location
     * GET /api/user/session/validate-location
     *
     * Kullanıcının lokasyonunun geçerli olup olmadığını kontrol eder
     */
    @GET(ApiConstants.SESSION_VALIDATE_LOCATION)
    suspend fun validateUserLocation(): Response<GetResponse<LocationValidationDto>>
}