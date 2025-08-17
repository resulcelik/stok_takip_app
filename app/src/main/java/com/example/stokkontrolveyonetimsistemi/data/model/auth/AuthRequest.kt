package com.example.stokkontrolveyonetimsistemi.data.model.auth

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Authentication request models for different auth operations
 * JWT authentication, password operations için request modelleri
 */

/**
 * Login request model
 * POST /api/auth/login için kullanılır
 */
@Keep
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

/**
 * Change password request model
 * POST /api/auth/change-password için kullanılır
 */
@Keep
data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,

    @SerializedName("newPassword")
    val newPassword: String,

    @SerializedName("newPasswordConfirm")
    val newPasswordConfirm: String
)

/**
 * Password reset request model
 * POST /api/auth/resetpassword için kullanılır
 */
@Keep
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("newPassword")
    val newPassword: String,

    @SerializedName("newPasswordConfirm")
    val newPasswordConfirm: String
)

/**
 * Send reset email request
 * POST /api/auth/sendpasswordresetemail için kullanılır
 */
@Keep
data class SendResetEmailRequest(
    @SerializedName("email")
    val email: String
)