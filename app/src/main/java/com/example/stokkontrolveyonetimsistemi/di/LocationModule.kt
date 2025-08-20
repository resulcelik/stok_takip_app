package com.example.stokkontrolveyonetimsistemi.di

import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.*
import com.example.stokkontrolveyonetimsistemi.presentation.location.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val locationModule = module {


    single<LocationRepository> {
        LocationRepository(
            locationApiService = get(),
            tokenStorage = get()
        )
    }


    single<GetLocationHierarchyUseCase> {
        GetLocationHierarchyUseCase(
            locationRepository = get()
        )
    }


    single<SetUserLocationUseCase> {
        SetUserLocationUseCase(
            locationRepository = get(),
            userRepository = get() // ✅ NEW: UserRepository eklendi
        )
    }


    single<GetStokBirimiUseCase> {
        GetStokBirimiUseCase(
            locationRepository = get()
        )
    }


    viewModel<LocationViewModel> {
        LocationViewModel(
            getLocationHierarchyUseCase = get(),
            setUserLocationUseCase = get()
        )
    }
}