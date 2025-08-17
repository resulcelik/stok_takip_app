package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.network.api.RafEtiketApiService
import com.example.stokkontrolveyonetimsistemi.data.repository.RafEtiketRepository
import com.example.stokkontrolveyonetimsistemi.presentation.rafetiket.RafEtiketViewModel
import com.example.stokkontrolveyonetimsistemi.printer.TscAlpha3RPrinterService
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * RAF Etiket Module - Dependency Injection
 * Bluetooth printer desteği ile
 */
val rafEtiketModule = module {

    // API Service
    single<RafEtiketApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(RafEtiketApiService::class.java)
    }

    // Repository
    single<RafEtiketRepository> {
        RafEtiketRepository(
            apiService = get(),
            gson = Gson()
        )
    }

    // Printer Service - Context ile
    single<TscAlpha3RPrinterService> {
        TscAlpha3RPrinterService(androidContext())
    }

    // ViewModel
    viewModel<RafEtiketViewModel> {
        RafEtiketViewModel(
            repository = get(),
            printerService = get()
        )
    }
}