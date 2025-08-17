package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.repository.UserRepository
import com.example.stokkontrolveyonetimsistemi.domain.main.GetQuickStatsUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.GetUserInfoUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.SessionValidationUseCase
import com.example.stokkontrolveyonetimsistemi.presentation.main.DashboardViewModel
import com.example.stokkontrolveyonetimsistemi.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Main module for dependency injection
 * Dashboard ve settings functionality için DI module
 * ✅ UPDATED: UserRepository eklendi
 */
val mainModule = module {

    // ==========================================
    // REPOSITORY LAYER
    // ==========================================

    /**
     * ✅ NEW: User Repository
     * User session ve lokasyon yönetimi
     */
    single<UserRepository> {
        UserRepository(
            userApiService = get(),
            tokenStorage = get()
        )
    }

    // ==========================================
    // USE CASE LAYER
    // ==========================================

    /**
     * Get user information use case
     * TokenStorage'dan kullanıcı bilgilerini getir
     */
    single<GetUserInfoUseCase> {
        GetUserInfoUseCase(
            tokenStorage = get()
        )
    }

    /**
     * Get quick statistics use case
     * Dashboard için hızlı istatistikler
     */
    single<GetQuickStatsUseCase> {
        GetQuickStatsUseCase(
            tokenStorage = get()
        )
    }

    /**
     * Session validation use case
     * Token geçerliliği kontrolü ve oturum yönetimi
     */
    single<SessionValidationUseCase> {
        SessionValidationUseCase(
            tokenStorage = get()
        )
    }

    // ==========================================
    // VIEW MODEL LAYER
    // ==========================================

    /**
     * Dashboard ViewModel
     * ✅ UPDATED: UserRepository eklendi
     */
    viewModel<DashboardViewModel> {
        DashboardViewModel(
            getUserInfoUseCase = get(),
            getQuickStatsUseCase = get(),
            sessionValidationUseCase = get(),
            logoutUseCase = get(), // AuthModule'dan inject
            userRepository = get() // ✅ NEW: UserRepository eklendi
        )
    }

    /**
     * Settings ViewModel
     * Şifre değiştirme ve ayarlar için
     */
    viewModel<SettingsViewModel> {
        SettingsViewModel(
            changePasswordUseCase = get() // AuthModule'dan inject
        )
    }

    // Future ViewModels (gelecek modüller için hazırlık):
    // viewModel<EnhancedScannerViewModel> { EnhancedScannerViewModel(get(), get()) }
    // viewModel<ProductListViewModel> { ProductListViewModel(get(), get()) }
    // viewModel<InventoryViewModel> { InventoryViewModel(get(), get()) }
    // viewModel<ShelfGeneratorViewModel> { ShelfGeneratorViewModel(get(), get()) }
    // viewModel<ProductGeneratorViewModel> { ProductGeneratorViewModel(get(), get()) }
}