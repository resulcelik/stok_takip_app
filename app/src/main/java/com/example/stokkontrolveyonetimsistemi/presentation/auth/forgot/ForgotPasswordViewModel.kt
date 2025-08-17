package com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Forgot Password ViewModel
 * Multi-step password reset flow management
 * Step 1: Email input → Send reset code
 * Step 2: Verification code input
 * Step 3: New password input → Reset password
 */
class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }

    // ==========================================
    // UI STATE MANAGEMENT
    // ==========================================

    // Form input states
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Reset operation state
    private val _resetState = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetState: StateFlow<AuthState> = _resetState.asStateFlow()

    // ==========================================
    // FORM INPUT HANDLING
    // ==========================================

    /**
     * Set email address
     */
    fun setEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null // Clear error when user types
        )
    }

    /**
     * Set verification code
     */
    fun setVerificationCode(code: String) {
        // Only allow 6 digits
        val cleanCode = code.filter { it.isDigit() }.take(6)

        _uiState.value = _uiState.value.copy(
            verificationCode = cleanCode,
            codeError = null // Clear error when user types
        )
    }

    /**
     * Set new password
     */
    fun setNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            passwordError = null // Clear error when user types
        )
    }

    /**
     * Set confirm password
     */
    fun setConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null // Clear error when user types
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
     * Toggle confirm password visibility
     */
    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    /**
     * Set current step
     */
    fun setCurrentStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    // ==========================================
    // RESET OPERATIONS
    // ==========================================

    /**
     * Step 1: Send reset email
     */
    fun sendResetEmail() {
        val currentState = _uiState.value

        // Clear previous errors
        clearErrors()

        // Email validation
        if (!isEmailValid(currentState.email)) {
            return
        }

        Log.d(TAG, "Sending reset email to: ${currentState.email.take(3)}***")

        viewModelScope.launch {
            resetPasswordUseCase.sendResetEmail(currentState.email.trim()).collect { resetState ->
                _resetState.value = resetState

                when (resetState) {
                    is AuthState.Success -> {
                        Log.d(TAG, "Reset email sent successfully")
                        // Move to next step (code verification)
                        _uiState.value = _uiState.value.copy(
                            currentStep = 2,
                            generalError = null
                        )
                    }

                    is AuthState.Error -> {
                        Log.w(TAG, "Send reset email failed: ${resetState.message}")
                        setGeneralError(resetState.message)
                    }

                    else -> {
                        // Loading state handled by UI
                    }
                }
            }
        }
    }

    /**
     * Step 2: Verify code (optional manual verification)
     */
    fun verifyCode() {
        val currentState = _uiState.value

        // Code validation
        if (!isCodeValid(currentState.verificationCode)) {
            return
        }

        Log.d(TAG, "Code verified, moving to password reset step")

        // Move to password reset step
        _uiState.value = _uiState.value.copy(
            currentStep = 3,
            generalError = null
        )
    }

    /**
     * Step 3: Reset password with code
     */
    fun resetPassword() {
        val currentState = _uiState.value

        // Clear previous errors
        clearErrors()

        // Full validation
        if (!isResetFormValid(currentState)) {
            return
        }

        Log.d(TAG, "Resetting password with verification code")

        viewModelScope.launch {
            resetPasswordUseCase.resetPassword(
                email = currentState.email.trim(),
                code = currentState.verificationCode.trim(),
                newPassword = currentState.newPassword,
                confirmPassword = currentState.confirmPassword
            ).collect { resetState ->
                _resetState.value = resetState

                when (resetState) {
                    is AuthState.Success -> {
                        Log.d(TAG, "Password reset successful")
                        // Mark as completed
                        _uiState.value = _uiState.value.copy(
                            isCompleted = true,
                            currentStep = 4,
                            generalError = null
                        )
                    }

                    is AuthState.Error -> {
                        Log.w(TAG, "Password reset failed: ${resetState.message}")
                        setGeneralError(resetState.message)
                    }

                    else -> {
                        // Loading state handled by UI
                    }
                }
            }
        }
    }

    // ==========================================
    // VALIDATION METHODS
    // ==========================================

    /**
     * Validate email
     */
    private fun isEmailValid(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(emailError = "E-posta adresi boş olamaz")
                false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(emailError = "Geçerli bir e-posta adresi girin")
                false
            }

            else -> true
        }
    }

    /**
     * Validate verification code
     */
    private fun isCodeValid(code: String): Boolean {
        return when {
            code.isBlank() -> {
                _uiState.value = _uiState.value.copy(codeError = "Doğrulama kodu boş olamaz")
                false
            }

            code.length != 6 -> {
                _uiState.value = _uiState.value.copy(codeError = "Doğrulama kodu 6 haneli olmalıdır")
                false
            }

            !code.all { it.isDigit() } -> {
                _uiState.value = _uiState.value.copy(codeError = "Doğrulama kodu sadece rakam içermelidir")
                false
            }

            else -> true
        }
    }

    /**
     * Validate complete reset form
     */
    private fun isResetFormValid(state: ForgotPasswordUiState): Boolean {
        var isValid = true

        // Email validation
        if (!isEmailValid(state.email)) {
            isValid = false
        }

        // Code validation
        if (!isCodeValid(state.verificationCode)) {
            isValid = false
        }

        // Password validation
        if (state.newPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Yeni şifre boş olamaz")
            isValid = false
        } else if (state.newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Yeni şifre en az 6 karakter olmalıdır")
            isValid = false
        }

        // Confirm password validation
        if (state.confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Şifre tekrarı boş olamaz")
            isValid = false
        } else if (state.confirmPassword != state.newPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Şifreler eşleşmiyor")
            isValid = false
        }

        return isValid
    }

    // ==========================================
    // ERROR HANDLING
    // ==========================================

    /**
     * Clear all errors
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            emailError = null,
            codeError = null,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
    }

    /**
     * Set general error message
     */
    private fun setGeneralError(message: String) {
        _uiState.value = _uiState.value.copy(generalError = message)
    }

    // ==========================================
    // STATE MANAGEMENT
    // ==========================================

    /**
     * Reset all states
     */
    fun resetStates() {
        _resetState.value = AuthState.Idle
        clearErrors()
    }

    /**
     * Reset to initial state (for retry)
     */
    fun resetToInitial() {
        _uiState.value = ForgotPasswordUiState()
        _resetState.value = AuthState.Idle
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ForgotPasswordViewModel cleared")

        // Clear sensitive data
        _uiState.value = _uiState.value.copy(
            newPassword = "",
            confirmPassword = "",
            verificationCode = ""
        )
    }
}

/**
 * Forgot Password UI State
 * Multi-step form state management
 */
data class ForgotPasswordUiState(
    // Current step (1: Email, 2: Code, 3: Password, 4: Success)
    val currentStep: Int = 1,

    // Form inputs
    val email: String = "",
    val verificationCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",

    // UI flags
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isCompleted: Boolean = false,

    // Form validation errors
    val emailError: String? = null,
    val codeError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null
) {
    /**
     * Check if current step can proceed
     */
    fun canProceed(): Boolean {
        return when (currentStep) {
            1 -> email.isNotBlank() &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                    emailError == null

            2 -> verificationCode.length == 6 &&
                    verificationCode.all { it.isDigit() } &&
                    codeError == null

            3 -> newPassword.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    newPassword.length >= 6 &&
                    newPassword == confirmPassword &&
                    passwordError == null &&
                    confirmPasswordError == null

            else -> false
        }
    }

    /**
     * Get current step title
     */
    fun getStepTitle(): String {
        return when (currentStep) {
            1 -> "E-posta Adresinizi Girin"
            2 -> "Doğrulama Kodunu Girin"
            3 -> "Yeni Şifrenizi Belirleyin"
            4 -> "Şifre Başarıyla Değiştirildi"
            else -> "Şifre Sıfırlama"
        }
    }

    /**
     * Get current step description
     */
    fun getStepDescription(): String {
        return when (currentStep) {
            1 -> "Size 6 haneli doğrulama kodu göndereceğiz"
            2 -> "E-posta adresinize gönderilen 6 haneli kodu girin"
            3 -> "Yeni şifrenizi belirleyin ve onaylayın"
            4 -> "Artık yeni şifrenizle giriş yapabilirsiniz"
            else -> ""
        }
    }
}