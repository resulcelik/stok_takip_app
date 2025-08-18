package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.network.api.MobileApiService
import com.example.stokkontrolveyonetimsistemi.data.repository.MobileRegistrationRepository
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Mobile Terminal Operations Module
 * Ürün ekleme workflow'u için DI konfigürasyonu
 */
val mobileModule = module {

    // API Service
    single<MobileApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(MobileApiService::class.java)
    }

    // Repository
    single {
        MobileRegistrationRepository(
            mobileApiService = get(),
            userRepository = get(),      // Session kontrolü için
            locationRepository = get(),   // Stok birimi dropdown için
            tokenStorage = get()
        )
    }

    // ViewModel
    viewModel {
        MobileRegistrationViewModel(
            repository = get()
        )
    }
}