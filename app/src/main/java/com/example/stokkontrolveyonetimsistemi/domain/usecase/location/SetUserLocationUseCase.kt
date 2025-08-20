package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import com.example.stokkontrolveyonetimsistemi.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetUserLocationUseCase(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository // ✅ NEW: UserRepository eklendi
) {

    companion object {
        private const val TAG = "SetUserLocationUseCase"
    }

    suspend fun execute(bolgeId: Long, depoId: Long): Flow<LocationResult<String>> = flow {
        try {
            Log.d(TAG, "Setting user location: bolgeId=$bolgeId, depoId=$depoId")

            // Input validation
            val validation = validateLocationInput(bolgeId, depoId)
            if (validation is LocationResult.Error) {
                emit(validation)
                return@flow
            }

            // Authentication check
            if (!userRepository.isUserAuthenticated()) {
                Log.w(TAG, "User not authenticated for location setting")
                emit(LocationResult.TokenExpired)
                return@flow
            }

            // ✅ UPDATED: UserRepository kullan
            // Execute location setting via UserRepository
            userRepository.setUserLocation(bolgeId, depoId).collect { result ->
                when (result) {
                    is LocationResult.Success -> {
                        Log.d(TAG, "Location set successfully: ${result.data}")
                        emit(LocationResult.Success(result.data))
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Location setting failed: ${result.message}")
                        emit(LocationResult.Error(result.message))
                    }
                    is LocationResult.TokenExpired -> {
                        Log.w(TAG, "Token expired during location setting")
                        emit(LocationResult.TokenExpired)
                    }
                    is LocationResult.Loading -> {
                        emit(LocationResult.Loading)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in location setting", e)
            emit(LocationResult.Error("Lokasyon ayarlama hatası: ${e.message}"))
        }
    }

    private fun validateLocationInput(bolgeId: Long, depoId: Long): LocationResult<Boolean> {
        return when {
            bolgeId <= 0 -> LocationResult.Error("Geçersiz bölge ID")
            depoId <= 0 -> LocationResult.Error("Geçersiz depo ID")
            else -> LocationResult.Success(true)
        }
    }
}