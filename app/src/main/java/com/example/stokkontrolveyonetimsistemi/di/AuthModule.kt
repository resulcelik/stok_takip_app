package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.network.api.AuthApiService
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.*
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginViewModel
import com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot.ForgotPasswordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Authentication module for dependency injection
 * Koin kullanarak authentication katmanının DI konfigürasyonu
 * Repository → UseCase → ViewModel hierarchy
 */
val authModule = module {

    // ==========================================
    // API SERVICE LAYER
    // ==========================================

    /**
     * Authentication API service
     * Retrofit instance'ından oluşturuluyor
     */
    single<AuthApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApiService::class.java)
    }

    // ==========================================
    // REPOSITORY LAYER
    // ==========================================

    /**
     * Authentication repository
     * API service + TokenStorage injection
     */
    single<AuthRepository> {
        AuthRepository(
            authApiService = get(),
            tokenStorage = get()
        )
    }

    // ==========================================
    // USE CASE LAYER
    // ==========================================

    /**
     * Login use case
     * Login işlemleri için business logic
     */
    single<LoginUseCase> {
        LoginUseCase(
            authRepository = get(),
            tokenStorage = get()
        )
    }

    /**
     * Auto login use case
     * Otomatik giriş kontrolü için business logic
     */
    single<AutoLoginUseCase> {
        AutoLoginUseCase(
            authRepository = get(),
            tokenStorage = get()
        )
    }

    /**
     * Logout use case
     * Çıkış işlemleri için business logic
     */
    single<LogoutUseCase> {
        LogoutUseCase(
            tokenStorage = get()
        )
    }

    /**
     * Password reset use case
     * Şifre sıfırlama işlemleri için business logic
     */
    single<ResetPasswordUseCase> {
        ResetPasswordUseCase(
            authRepository = get()
        )
    }

    /**
     * Password change use case
     * Şifre değiştirme işlemleri için business logic
     */
    single<ChangePasswordUseCase> {
        ChangePasswordUseCase(
            authRepository = get()
        )
    }

    // ==========================================
    // VIEW MODEL LAYER
    // ==========================================

    /**
     * Login ViewModel
     * Login screen için UI state management
     */
    viewModel<LoginViewModel> {
        LoginViewModel(
            loginUseCase = get(),
            autoLoginUseCase = get(),
            resetPasswordUseCase = get()
        )
    }

    /**
     * Forgot Password ViewModel
     * Multi-step password reset flow için UI state management
     */
    viewModel<ForgotPasswordViewModel> {
        ForgotPasswordViewModel(
            resetPasswordUseCase = get()
        )
    }

    // Future ViewModels (gelecek modüller için hazırlık):
    // viewModel<SplashViewModel> { SplashViewModel(get()) }
    // viewModel<ChangePasswordViewModel> { ChangePasswordViewModel(get()) }
    // viewModel<ProfileViewModel> { ProfileViewModel(get(), get()) }
}