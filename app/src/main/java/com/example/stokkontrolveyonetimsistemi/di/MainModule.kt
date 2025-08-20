package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.repository.UserRepository
import com.example.stokkontrolveyonetimsistemi.domain.main.GetQuickStatsUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.GetUserInfoUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.SessionValidationUseCase
import com.example.stokkontrolveyonetimsistemi.presentation.main.DashboardViewModel
import com.example.stokkontrolveyonetimsistemi.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val mainModule = module {


    single<UserRepository> {
        UserRepository(
            userApiService = get(),
            tokenStorage = get()
        )
    }

    single<GetUserInfoUseCase> {
        GetUserInfoUseCase(
            tokenStorage = get()
        )
    }

    single<GetQuickStatsUseCase> {
        GetQuickStatsUseCase(
            tokenStorage = get()
        )
    }

    single<SessionValidationUseCase> {
        SessionValidationUseCase(
            tokenStorage = get()
        )
    }


    viewModel<DashboardViewModel> {
        DashboardViewModel(
            getUserInfoUseCase = get(),
            getQuickStatsUseCase = get(),
            sessionValidationUseCase = get(),
            logoutUseCase = get(), // AuthModule'dan inject
            userRepository = get() // ✅ NEW: UserRepository eklendi
        )
    }

    viewModel<SettingsViewModel> {
        SettingsViewModel(
            changePasswordUseCase = get() // AuthModule'dan inject
        )
    }
}