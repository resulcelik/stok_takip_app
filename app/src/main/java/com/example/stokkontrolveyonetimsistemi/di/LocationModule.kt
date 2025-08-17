package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.*
import com.example.stokkontrolveyonetimsistemi.presentation.location.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Location module for dependency injection
 * Location cascade dropdown ve session location management için DI module
 *
 * ✅ NEW: SetUserLocationUseCase'e UserRepository eklendi
 */
val locationModule = module {

    // ==========================================
    // REPOSITORY LAYER
    // ==========================================

    /**
     * Location Repository
     * Cascade dropdown operations için
     */
    single<LocationRepository> {
        LocationRepository(
            locationApiService = get(),
            tokenStorage = get()
        )
    }

    // ==========================================
    // USE CASE LAYER
    // ==========================================

    /**
     * Get Location Hierarchy Use Case
     * Cascade dropdown data loading
     */
    single<GetLocationHierarchyUseCase> {
        GetLocationHierarchyUseCase(
            locationRepository = get()
        )
    }

    /**
     * Set User Location Use Case
     * ✅ UPDATED: UserRepository eklendi
     */
    single<SetUserLocationUseCase> {
        SetUserLocationUseCase(
            locationRepository = get(),
            userRepository = get() // ✅ NEW: UserRepository eklendi
        )
    }

    /**
     * Get Stok Birimi Use Case
     * Product form dropdown data
     */
    single<GetStokBirimiUseCase> {
        GetStokBirimiUseCase(
            locationRepository = get()
        )
    }

    // ==========================================
    // VIEW MODEL LAYER
    // ==========================================

    /**
     * Location ViewModel
     * Location selection screen için
     */
    viewModel<LocationViewModel> {
        LocationViewModel(
            getLocationHierarchyUseCase = get(),
            setUserLocationUseCase = get()
        )
    }
}