package com.example.stokkontrolveyonetimsistemi.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.AutoLoginUseCase
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.LoginUseCase
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Login screen ViewModel
 * Login form state management ve authentication logic
 * Modüler reactive approach ile UI state yönetimi
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val autoLoginUseCase: AutoLoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    // ==========================================
    // UI STATE MANAGEMENT
    // ==========================================

    // Form input states
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Password reset state
    private val _resetState = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetState: StateFlow<AuthState> = _resetState.asStateFlow()

    init {
        // Load last login info if available
        loadLastLoginInfo()
    }

    // ==========================================
    // FORM INPUT HANDLING
    // ==========================================

    /**
     * Update username input
     */
    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null // Clear error when user types
        )
    }

    /**
     * Update password input
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null // Clear error when user types
        )
    }

    /**
     * Toggle password visibility
     */
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    /**
     * Toggle remember me checkbox
     */
    fun toggleRememberMe() {
        _uiState.value = _uiState.value.copy(
            rememberMe = !_uiState.value.rememberMe
        )
    }

    // ==========================================
    // AUTHENTICATION OPERATIONS
    // ==========================================

    /**
     * Perform login operation
     */
    fun login() {
        val currentState = _uiState.value

        // Clear previous errors
        clearFormErrors()

        // Basic client-side validation
        if (!isFormValid(currentState)) {
            return
        }

        Log.d(TAG, "Login initiated for user: ${currentState.username.take(3)}***")

        viewModelScope.launch {
            loginUseCase.execute(
                username = currentState.username.trim(),
                password = currentState.password,
                rememberMe = currentState.rememberMe
            ).collect { authState ->
                _authState.value = authState
                handleAuthResult(authState)
            }
        }
    }

    /**
     * Handle authentication result
     */
    private fun handleAuthResult(authState: AuthState) {
        when (authState) {
            is AuthState.Success -> {
                Log.d(TAG, "Login successful")
                clearSensitiveData()
            }

            is AuthState.Error -> {
                Log.w(TAG, "Login failed: ${authState.message}")
                setFormError(authState.message)
            }

            is AuthState.TokenExpired -> {
                Log.w(TAG, "Token expired during login")
                setFormError("Oturum süresi dolmuş, lütfen tekrar giriş yapın")
            }

            else -> {
                // Loading state - UI will handle this
            }
        }
    }

    // ==========================================
    // PASSWORD RESET OPERATIONS
    // ==========================================

    /**
     * Send password reset email
     */
    fun sendResetEmail(email: String) {
        Log.d(TAG, "Sending reset email for: ${email.take(3)}***${email.takeLast(3)}")

        viewModelScope.launch {
            resetPasswordUseCase.sendResetEmail(email).collect { resetState ->
                _resetState.value = resetState
            }
        }
    }

    /**
     * Reset password with verification code
     */
    fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String
    ) {
        Log.d(TAG, "Resetting password for: ${email.take(3)}***${email.takeLast(3)}")

        viewModelScope.launch {
            resetPasswordUseCase.resetPassword(email, code, newPassword, confirmPassword)
                .collect { resetState ->
                    _resetState.value = resetState
                }
        }
    }

    // ==========================================
    // FORM VALIDATION
    // ==========================================

    /**
     * Validate form inputs
     */
    private fun isFormValid(state: LoginUiState): Boolean {
        var isValid = true

        // Username validation
        if (state.username.isBlank()) {
            _uiState.value = _uiState.value.copy(usernameError = "Kullanıcı adı boş olamaz")
            isValid = false
        } else if (state.username.length < 3) {
            _uiState.value = _uiState.value.copy(usernameError = "Kullanıcı adı çok kısa")
            isValid = false
        }

        // Password validation
        if (state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Şifre boş olamaz")
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Şifre en az 6 karakter olmalıdır")
            isValid = false
        }

        return isValid
    }

    /**
     * Clear form validation errors
     */
    private fun clearFormErrors() {
        _uiState.value = _uiState.value.copy(
            usernameError = null,
            passwordError = null,
            generalError = null
        )
    }

    /**
     * Set general form error
     */
    private fun setFormError(message: String) {
        _uiState.value = _uiState.value.copy(
            generalError = message
        )
    }

    // ==========================================
    // DATA MANAGEMENT
    // ==========================================

    /**
     * Load last login information
     */
    private fun loadLastLoginInfo() {
        try {
            val lastLoginInfo = autoLoginUseCase.getLastLoginInfo()

            if (lastLoginInfo != null) {
                _uiState.value = _uiState.value.copy(
                    username = lastLoginInfo.username,
                    rememberMe = lastLoginInfo.rememberMe
                )

                Log.d(TAG, "Last login info loaded for user: ${lastLoginInfo.username.take(3)}***")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load last login info", e)
        }
    }

    /**
     * Clear sensitive data from memory
     */
    private fun clearSensitiveData() {
        _uiState.value = _uiState.value.copy(
            password = "", // Clear password from memory
            passwordError = null,
            generalError = null
        )
    }

    // ==========================================
    // STATE RESET OPERATIONS
    // ==========================================

    /**
     * Reset authentication state
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Reset password reset state
     */
    fun resetPasswordResetState() {
        _resetState.value = AuthState.Idle
    }

    /**
     * Reset all states
     */
    fun resetAllStates() {
        resetAuthState()
        resetPasswordResetState()
        clearFormErrors()
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "LoginViewModel cleared")

        // Clear sensitive data when ViewModel is destroyed
        clearSensitiveData()
    }
}

/**
 * Login UI state data class
 * Form state management için immutable state
 */
data class LoginUiState(
    // Form inputs
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,

    // Form validation errors
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,

    // UI state flags
    val isLoading: Boolean = false,
    val isFormEnabled: Boolean = true
) {
    /**
     * Check if form can be submitted
     */
    fun canSubmit(): Boolean {
        return username.isNotBlank() &&
                password.isNotBlank() &&
                isFormEnabled &&
                !isLoading &&
                usernameError == null &&
                passwordError == null
    }
}