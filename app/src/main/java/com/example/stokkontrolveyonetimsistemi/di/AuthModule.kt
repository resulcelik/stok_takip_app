package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.network.auth.api.AuthApiService
import com.example.stokkontrolveyonetimsistemi.data.repository.AuthRepository
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.*
import com.example.stokkontrolveyonetimsistemi.presentation.auth.login.LoginViewModel
import com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot.ForgotPasswordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit


val authModule = module {


    single<AuthApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApiService::class.java)
    }

    single<AuthRepository> {
        AuthRepository(
            authApiService = get(),
            tokenStorage = get()
        )
    }


    single<LoginUseCase> {
        LoginUseCase(
            authRepository = get(),
            tokenStorage = get()
        )
    }

    single<AutoLoginUseCase> {
        AutoLoginUseCase(
            authRepository = get(),
            tokenStorage = get()
        )
    }

    single<LogoutUseCase> {
        LogoutUseCase(
            tokenStorage = get()
        )
    }

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


    viewModel<LoginViewModel> {
        LoginViewModel(
            loginUseCase = get(),
            autoLoginUseCase = get(),
        )
    }


    viewModel<ForgotPasswordViewModel> {
        ForgotPasswordViewModel(
            resetPasswordUseCase = get()
        )
    }
}