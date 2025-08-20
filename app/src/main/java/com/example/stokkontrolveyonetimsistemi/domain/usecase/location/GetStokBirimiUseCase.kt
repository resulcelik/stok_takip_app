package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import kotlinx.coroutines.flow.Flow


class GetStokBirimiUseCase(
    private val locationRepository: LocationRepository
) {

    companion object {
        private const val TAG = "GetStokBirimiUseCase"
    }

    suspend fun execute(): Flow<LocationResult<List<StokBirimiDto>>> {
        Log.d(TAG, "Loading stock units for product form")
        return locationRepository.getAllStokBirimi()
    }
}