package com.example.stokkontrolveyonetimsistemi.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.ChangePasswordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Settings ViewModel - Clean Password Management
 * ✅ CLEANED: Pure password management, location logic removed
 *
 * Dosya Konumu: /presentation/settings/SettingsViewModel.kt
 * Pattern: Single responsibility - sadece şifre değiştirme
 * DI: MainModule.kt'de registered
 * Features: Password change with backend API integration
 */
class SettingsViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    // ==========================================
    // PASSWORD CHANGE STATE MANAGEMENT
    // ==========================================

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    // ==========================================
    // PASSWORD CHANGE OPERATIONS
    // ==========================================

    /**
     * Change password with backend API integration
     * Şifre değiştirme işlemi - backend API call
     */
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting password change process")

                // Client-side validation first
                val validationError = validatePasswordChange(currentPassword, newPassword, confirmPassword)
                if (validationError != null) {
                    _passwordChangeState.value = PasswordChangeState.Error(validationError)
                    return@launch
                }

                // Call backend API through use case
                changePasswordUseCase.execute(currentPassword, newPassword, confirmPassword)
                    .collect { authState ->
                        when (authState) {
                            is AuthState.Loading -> {
                                Log.d(TAG, "Password change in progress")
                                _passwordChangeState.value = PasswordChangeState.Loading
                            }
                            is AuthState.Success -> {
                                Log.d(TAG, "Password change successful")
                                _passwordChangeState.value = PasswordChangeState.Success(
                                    authState.message ?: "Şifre başarıyla değiştirildi"
                                )
                            }
                            is AuthState.Error -> {
                                Log.e(TAG, "Password change failed: ${authState.message}")
                                _passwordChangeState.value = PasswordChangeState.Error(authState.message)
                            }
                            else -> {
                                Log.w(TAG, "Unexpected auth state: $authState")
                            }
                        }
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Password change error", e)
                _passwordChangeState.value = PasswordChangeState.Error("Şifre değiştirme hatası: ${e.message}")
            }
        }
    }

    // ==========================================
    // VALIDATION OPERATIONS
    // ==========================================

    /**
     * Password change validation
     * Client-side validation rules
     */
    private fun validatePasswordChange(current: String, new: String, confirm: String): String? {
        return when {
            current.isBlank() -> "Mevcut şifre boş olamaz"
            new.isBlank() -> "Yeni şifre boş olamaz"
            confirm.isBlank() -> "Şifre tekrarı boş olamaz"
            new.length < 6 -> "Yeni şifre en az 6 karakter olmalı"
            new != confirm -> "Yeni şifre ve tekrarı eşleşmiyor"
            current == new -> "Yeni şifre eskisiyle aynı olamaz"
            else -> null
        }
    }

    // ==========================================
    // STATE RESET OPERATIONS
    // ==========================================

    /**
     * Reset password change state
     * UI state'ini idle'a çevir
     */
    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "SettingsViewModel cleared")
    }
}

// ==========================================
// PASSWORD CHANGE STATE CLASSES
// ==========================================

/**
 * Password change states
 * Şifre değiştirme işlemi durumları
 */
sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    data class Success(val message: String) : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}