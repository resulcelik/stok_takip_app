package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Authentication API service interface
 * Spring Boot backend authentication endpoints
 *
 * Backend URL pattern: http://192.168.1.100:8080/api/auth/
 */
interface AuthApiService {

    /**
     * User login authentication
     * POST /api/auth/login
     *
     * @param request Login credentials (username, password)
     * @return JWT token response or error
     */
    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * Send password reset email with 6-digit code
     * POST /api/auth/sendpasswordresetemail
     * Query parameter: ?email=user@example.com
     *
     * @param email User email address
     * @return Success/error message
     */
    @POST(ApiConstants.AUTH_SEND_RESET_EMAIL)
    suspend fun sendResetEmail(
        @Query("email") email: String
    ): Response<MessageResponse>

    /**
     * Reset password with email verification code
     * POST /api/auth/resetpassword
     *
     * @param request Reset password request with email, code, and new passwords
     * @return Success/error message
     */
    @POST(ApiConstants.AUTH_RESET_PASSWORD)
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>

    /**
     * Change password for authenticated user
     * POST /api/auth/change-password
     * Requires: Authorization: Bearer {jwt_token}
     *
     * @param request Current password and new password
     * @return Success/error message
     */
    @POST(ApiConstants.AUTH_CHANGE_PASSWORD)
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>

    /**
     * Validate current JWT token (implicit)
     * Any authenticated endpoint can be used for token validation
     * This is a utility method that uses change-password endpoint with empty body
     * to check if token is still valid
     *
     * @return Response indicating token validity
     */
    @GET(ApiConstants.AUTH_BASE + "validate")
    suspend fun validateToken(): Response<MessageResponse>
}