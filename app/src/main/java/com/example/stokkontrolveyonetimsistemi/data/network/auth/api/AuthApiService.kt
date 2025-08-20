package com.example.stokkontrolveyonetimsistemi.data.network.auth.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>


    @POST(ApiConstants.AUTH_SEND_RESET_EMAIL)
    suspend fun sendResetEmail(
        @Query("email") email: String
    ): Response<MessageResponse>


    @POST(ApiConstants.AUTH_RESET_PASSWORD)
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>


    @POST(ApiConstants.AUTH_CHANGE_PASSWORD)
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>


    @GET(ApiConstants.AUTH_BASE + "validate")
    suspend fun validateToken(): Response<MessageResponse>
}