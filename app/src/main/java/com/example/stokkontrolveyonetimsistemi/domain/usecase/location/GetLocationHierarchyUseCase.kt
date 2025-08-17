package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Get Location Hierarchy Use Case
 * Cascade dropdown işlemleri için business logic
 *
 * Dosya Konumu: /domain/usecase/location/GetLocationHierarchyUseCase.kt
 * Pattern: Clean Architecture Use Case pattern
 */
class GetLocationHierarchyUseCase(
    private val locationRepository: LocationRepository
) {

    companion object {
        private const val TAG = "GetLocationHierarchyUseCase"
    }

    // ==========================================
    // HIERARCHICAL DATA LOADING
    // ==========================================

    /**
     * Get all regions (First level)
     * Cascade dropdown'ın ilk seviyesi
     */
    suspend fun getAllBolge(): Flow<LocationResult<List<BolgeDto>>> {
        Log.d(TAG, "Loading all regions")
        return locationRepository.getAllBolge()
    }

    /**
     * Get provinces by region (Second level)
     * Bölge seçildiğinde il listesini getir
     */
    suspend fun getIllerByBolge(bolgeId: Long): Flow<LocationResult<List<IlDto>>> {
        Log.d(TAG, "Loading provinces for region: $bolgeId")

        if (bolgeId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz bölge ID"))
            }
        }

        return locationRepository.getIllerByBolge(bolgeId)
    }

    /**
     * Get districts by province (Third level)
     * İl seçildiğinde ilçe listesini getir
     */
    suspend fun getIlcelerByIl(ilId: Long): Flow<LocationResult<List<IlceDto>>> {
        Log.d(TAG, "Loading districts for province: $ilId")

        if (ilId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz il ID"))
            }
        }

        return locationRepository.getIlcelerByIl(ilId)
    }

    /**
     * Get warehouses by district (Fourth level)
     * İlçe seçildiğinde depo listesini getir
     */
    suspend fun getDepolarByIlce(ilceId: Long): Flow<LocationResult<List<DepoDto>>> {
        Log.d(TAG, "Loading warehouses for district: $ilceId")

        if (ilceId <= 0) {
            return flow {
                emit(LocationResult.Error("Geçersiz ilçe ID"))
            }
        }

        return locationRepository.getDepolarByIlce(ilceId)
    }

    // ==========================================
    // COMPLEX BUSINESS OPERATIONS
    // ==========================================

    /**
     * Load complete location hierarchy
     * Tüm cascade verileri tek seferde yükle (performance optimization)
     */
    suspend fun loadCompleteLocationHierarchy(
        bolgeId: Long? = null,
        ilId: Long? = null,
        ilceId: Long? = null
    ): Flow<LocationResult<LocationSelectionState>> = flow {
        try {
            emit(LocationResult.Loading)

            Log.d(TAG, "Loading complete location hierarchy")

            var state = LocationSelectionState(isLoading = true)

            // 1. Load regions first
            locationRepository.getAllBolge().collect { bolgeResult ->
                when (bolgeResult) {
                    is LocationResult.Success -> {
                        state = state.copy(
                            availableBolgeler = bolgeResult.data,
                            selectedBolge = bolgeResult.data.find { it.id == bolgeId }
                        )

                        // 2. If bolgeId provided, load provinces
                        if (bolgeId != null) {
                            locationRepository.getIllerByBolge(bolgeId).collect { ilResult ->
                                when (ilResult) {
                                    is LocationResult.Success -> {
                                        state = state.copy(
                                            availableIller = ilResult.data,
                                            selectedIl = ilResult.data.find { it.id == ilId }
                                        )

                                        // 3. If ilId provided, load districts
                                        if (ilId != null) {
                                            locationRepository.getIlcelerByIl(ilId).collect { ilceResult ->
                                                when (ilceResult) {
                                                    is LocationResult.Success -> {
                                                        state = state.copy(
                                                            availableIlceler = ilceResult.data,
                                                            selectedIlce = ilceResult.data.find { it.id == ilceId }
                                                        )

                                                        // 4. If ilceId provided, load warehouses
                                                        if (ilceId != null) {
                                                            locationRepository.getDepolarByIlce(ilceId).collect { depoResult ->
                                                                when (depoResult) {
                                                                    is LocationResult.Success -> {
                                                                        state = state.copy(
                                                                            availableDepolar = depoResult.data,
                                                                            isLoading = false
                                                                        )
                                                                        emit(LocationResult.Success(state))
                                                                    }
                                                                    is LocationResult.Error -> {
                                                                        emit(LocationResult.Error(depoResult.message))
                                                                    }
                                                                    is LocationResult.TokenExpired -> {
                                                                        emit(LocationResult.TokenExpired)
                                                                    }
                                                                    else -> { /* Loading handled above */ }
                                                                }
                                                            }
                                                        } else {
                                                            state = state.copy(isLoading = false)
                                                            emit(LocationResult.Success(state))
                                                        }
                                                    }
                                                    is LocationResult.Error -> {
                                                        emit(LocationResult.Error(ilceResult.message))
                                                    }
                                                    is LocationResult.TokenExpired -> {
                                                        emit(LocationResult.TokenExpired)
                                                    }
                                                    else -> { /* Loading handled above */ }
                                                }
                                            }
                                        } else {
                                            state = state.copy(isLoading = false)
                                            emit(LocationResult.Success(state))
                                        }
                                    }
                                    is LocationResult.Error -> {
                                        emit(LocationResult.Error(ilResult.message))
                                    }
                                    is LocationResult.TokenExpired -> {
                                        emit(LocationResult.TokenExpired)
                                    }
                                    else -> { /* Loading handled above */ }
                                }
                            }
                        } else {
                            state = state.copy(isLoading = false)
                            emit(LocationResult.Success(state))
                        }
                    }
                    is LocationResult.Error -> {
                        emit(LocationResult.Error(bolgeResult.message))
                    }
                    is LocationResult.TokenExpired -> {
                        emit(LocationResult.TokenExpired)
                    }
                    else -> { /* Loading handled above */ }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading complete hierarchy", e)
            emit(LocationResult.Error("Lokasyon verileri yüklenemedi: ${e.message}"))
        }
    }

    // ==========================================
    // VALIDATION OPERATIONS
    // ==========================================

    /**
     * Validate cascade selection logic
     * Dropdown seçim kurallarını kontrol et
     */
    fun validateCascadeSelection(
        selectedBolge: BolgeDto?,
        selectedIl: IlDto?,
        selectedIlce: IlceDto?,
        selectedDepo: DepoDto?
    ): LocationResult<Boolean> {

        return when {
            // Basic validation
            selectedBolge == null -> LocationResult.Error("Bölge seçimi gerekli")

            // Hierarchy validation
            selectedIl != null && selectedIl.bolgeId != selectedBolge.id ->
                LocationResult.Error("Seçilen il, bölgeye ait değil")

            selectedIlce != null && selectedIl != null && selectedIlce.ilId != selectedIl.id ->
                LocationResult.Error("Seçilen ilçe, ile ait değil")

            selectedDepo != null && selectedIlce != null && selectedDepo.ilceId != selectedIlce.id ->
                LocationResult.Error("Seçilen depo, ilçeye ait değil")

            selectedDepo != null && !selectedDepo.isAvailable() ->
                LocationResult.Error("Seçilen depo aktif değil")

            // All good
            else -> LocationResult.Success(true)
        }
    }

    /**
     * Get minimum required selection for session
     * Session ayarlamak için minimum seçim: Bölge + Depo
     */
    fun getMinimumRequiredSelection(state: LocationSelectionState): LocationResult<SetLocationRequest> {
        val validation = validateCascadeSelection(
            state.selectedBolge,
            state.selectedIl,
            state.selectedIlce,
            state.selectedDepo
        )

        return when (validation) {
            is LocationResult.Error -> LocationResult.Error(validation.message)
            is LocationResult.Success -> {
                if (state.selectedBolge != null && state.selectedDepo != null) {
                    LocationResult.Success(
                        SetLocationRequest(
                            bolgeId = state.selectedBolge.id,
                            depoId = state.selectedDepo.id
                        )
                    )
                } else {
                    LocationResult.Error("Minimum bölge ve depo seçimi gerekli")
                }
            }
            else -> LocationResult.Error("Seçim validation hatası")
        }
    }
}