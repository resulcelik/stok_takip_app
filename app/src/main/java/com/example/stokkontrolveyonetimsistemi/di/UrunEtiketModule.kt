package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.network.api.UrunEtiketApiService
import com.example.stokkontrolveyonetimsistemi.data.repository.UrunEtiketRepository
import com.example.stokkontrolveyonetimsistemi.presentation.urun.UrunEtiketViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val urunEtiketModule = module {

    // ÜRÜN API Service
    single<UrunEtiketApiService> {
        get<Retrofit>().create(UrunEtiketApiService::class.java)
    }

    // ÜRÜN Repository
    single {
        UrunEtiketRepository(
            apiService = get()
        )
    }

    // ÜRÜN ViewModel
    viewModel {
        UrunEtiketViewModel(
            repository = get(),
            printerService = get()
        )
    }
}