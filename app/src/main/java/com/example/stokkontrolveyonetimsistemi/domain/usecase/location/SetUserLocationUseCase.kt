package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import com.example.stokkontrolveyonetimsistemi.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Set User Location Use Case
 * Session lokasyon ayarlama business logic
 *
 * ✅ UPDATED: UserRepository kullanacak şekilde güncellendi
 *
 * Dosya Konumu: /domain/usecase/location/SetUserLocationUseCase.kt
 * Business rule: Bölge + Depo seçimi yeterli (İl ve İlçe opsiyonel)
 */
class SetUserLocationUseCase(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository // ✅ NEW: UserRepository eklendi
) {

    companion object {
        private const val TAG = "SetUserLocationUseCase"
    }

    // ==========================================
    // MAIN OPERATIONS
    // ==========================================

    /**
     * Set user session location
     * Ana lokasyon ayarlama işlemi
     *
     * ✅ UPDATED: UserRepository.setUserLocation kullanılıyor
     *
     * @param bolgeId Selected region ID
     * @param depoId Selected warehouse ID
     * @return Success/error result with message
     */
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

    /**
     * Set location with state validation
     * LocationSelectionState'den lokasyon ayarla
     */
    suspend fun executeWithState(state: LocationSelectionState): Flow<LocationResult<String>> = flow {
        try {
            Log.d(TAG, "Setting location with state validation")

            // State validation
            if (!state.isSelectionComplete()) {
                emit(LocationResult.Error("Eksik lokasyon seçimi"))
                return@flow
            }

            val bolgeId = state.selectedBolge?.id
            val depoId = state.selectedDepo?.id

            if (bolgeId == null || depoId == null) {
                emit(LocationResult.Error("Bölge ve depo seçimi gerekli"))
                return@flow
            }

            // Repository validation (LocationRepository'deki validation metodunu kullan)
            val repoValidation = locationRepository.validateLocationSelection(
                state.selectedBolge,
                state.selectedDepo
            )

            when (repoValidation) {
                is LocationResult.Error -> {
                    emit(LocationResult.Error(repoValidation.message))
                    return@flow
                }
                is LocationResult.Success -> {
                    // Proceed with location setting
                    execute(bolgeId, depoId).collect { result ->
                        emit(result)
                    }
                }
                else -> {
                    emit(LocationResult.Error("Lokasyon validation hatası"))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in state-based location setting", e)
            emit(LocationResult.Error("Lokasyon ayarlama hatası: ${e.message}"))
        }
    }

    // ==========================================
    // BUSINESS LOGIC OPERATIONS
    // ==========================================

    /**
     * Get location hierarchy step by step
     * UI için aşamalı cascade loading
     *
     * LocationRepository'deki cascade metodlarını kullanır
     */
    suspend fun getLocationStep(
        currentLevel: LocationLevel,
        parentId: Long
    ): Flow<LocationResult<List<Any>>> = flow {
        try {
            Log.d(TAG, "Loading location step: $currentLevel for parent: $parentId")

            when (currentLevel) {
                LocationLevel.BOLGE -> {
                    locationRepository.getAllBolge().collect { result ->
                        when (result) {
                            is LocationResult.Success -> emit(LocationResult.Success(result.data))
                            is LocationResult.Error -> emit(LocationResult.Error(result.message))
                            is LocationResult.TokenExpired -> emit(LocationResult.TokenExpired)
                            is LocationResult.Loading -> emit(LocationResult.Loading)
                        }
                    }
                }
                LocationLevel.IL -> {
                    locationRepository.getIllerByBolge(parentId).collect { result ->
                        when (result) {
                            is LocationResult.Success -> emit(LocationResult.Success(result.data))
                            is LocationResult.Error -> emit(LocationResult.Error(result.message))
                            is LocationResult.TokenExpired -> emit(LocationResult.TokenExpired)
                            is LocationResult.Loading -> emit(LocationResult.Loading)
                        }
                    }
                }
                LocationLevel.ILCE -> {
                    locationRepository.getIlcelerByIl(parentId).collect { result ->
                        when (result) {
                            is LocationResult.Success -> emit(LocationResult.Success(result.data))
                            is LocationResult.Error -> emit(LocationResult.Error(result.message))
                            is LocationResult.TokenExpired -> emit(LocationResult.TokenExpired)
                            is LocationResult.Loading -> emit(LocationResult.Loading)
                        }
                    }
                }
                LocationLevel.DEPO -> {
                    locationRepository.getDepolarByIlce(parentId).collect { result ->
                        when (result) {
                            is LocationResult.Success -> emit(LocationResult.Success(result.data))
                            is LocationResult.Error -> emit(LocationResult.Error(result.message))
                            is LocationResult.TokenExpired -> emit(LocationResult.TokenExpired)
                            is LocationResult.Loading -> emit(LocationResult.Loading)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading location step", e)
            emit(LocationResult.Error("Lokasyon verisi yüklenemedi: ${e.message}"))
        }
    }

    // ==========================================
    // VALIDATION UTILITIES
    // ==========================================

    /**
     * Validate location input parameters
     */
    private fun validateLocationInput(bolgeId: Long, depoId: Long): LocationResult<Boolean> {
        return when {
            bolgeId <= 0 -> LocationResult.Error("Geçersiz bölge ID")
            depoId <= 0 -> LocationResult.Error("Geçersiz depo ID")
            else -> LocationResult.Success(true)
        }
    }

    /**
     * Check if location hierarchy is logically consistent
     * LocationRepository'deki validation metodunu kullanır
     */
    fun validateLocationHierarchy(
        bolge: BolgeDto?,
        il: IlDto?,
        ilce: IlceDto?,
        depo: DepoDto?
    ): LocationResult<Boolean> {
        // Use LocationRepository validation for cascade logic
        return locationRepository.validateLocationSelection(bolge, depo)
    }
}

// ==========================================
// HELPER ENUMS
// ==========================================

/**
 * Location hierarchy levels
 * Cascade dropdown seviyelerini tanımlar
 */
enum class LocationLevel {
    BOLGE,   // Region (1st level)
    IL,      // Province (2nd level)
    ILCE,    // District (3rd level)
    DEPO     // Warehouse (4th level)
}