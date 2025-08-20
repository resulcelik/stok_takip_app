package com.example.stokkontrolveyonetimsistemi.presentation.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.GetLocationHierarchyUseCase
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.SetUserLocationUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocationViewModel(
    private val getLocationHierarchyUseCase: GetLocationHierarchyUseCase,
    private val setUserLocationUseCase: SetUserLocationUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LocationViewModel"
    }

    private val _uiState = MutableStateFlow(LocationSelectionState())
    val uiState: StateFlow<LocationSelectionState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LocationNavigationEvent>()
    val navigationEvent: SharedFlow<LocationNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        Log.d(TAG, "LocationViewModel initialized")
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading initial regions data")
                loadBolgeler()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load initial data", e)
                handleError("İlk veriler yüklenemedi: ${e.message}")
            }
        }
    }

    private fun loadBolgeler() {
        viewModelScope.launch {
            getLocationHierarchyUseCase.getAllBolge().collect { result ->
                when (result) {
                    is LocationResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is LocationResult.Success -> {
                        Log.d(TAG, "Regions loaded: ${result.data.size} items")
                        _uiState.value = _uiState.value.copy(
                            availableBolgeler = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Failed to load regions: ${result.message}")
                        handleError("Bölge listesi yüklenemedi: ${result.message}")
                    }
                    is LocationResult.TokenExpired -> {
                        Log.w(TAG, "Token expired while loading regions")
                        handleTokenExpired()
                    }
                }
            }
        }
    }

    private fun loadIllerByBolge(bolgeId: Long) {
        viewModelScope.launch {
            getLocationHierarchyUseCase.getIllerByBolge(bolgeId).collect { result ->
                when (result) {
                    is LocationResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is LocationResult.Success -> {
                        Log.d(TAG, "Provinces loaded: ${result.data.size} items for region $bolgeId")
                        _uiState.value = _uiState.value.copy(
                            availableIller = result.data,
                            isLoading = false,
                            errorMessage = null,
                            // Clear child selections
                            selectedIl = null,
                            selectedIlce = null,
                            selectedDepo = null,
                            availableIlceler = emptyList(),
                            availableDepolar = emptyList()
                        )
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Failed to load provinces: ${result.message}")
                        handleError("İl listesi yüklenemedi: ${result.message}")
                    }
                    is LocationResult.TokenExpired -> {
                        Log.w(TAG, "Token expired while loading provinces")
                        handleTokenExpired()
                    }
                }
            }
        }
    }


    private fun loadIlcelerByIl(ilId: Long) {
        viewModelScope.launch {
            getLocationHierarchyUseCase.getIlcelerByIl(ilId).collect { result ->
                when (result) {
                    is LocationResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is LocationResult.Success -> {
                        Log.d(TAG, "Districts loaded: ${result.data.size} items for province $ilId")
                        _uiState.value = _uiState.value.copy(
                            availableIlceler = result.data,
                            isLoading = false,
                            errorMessage = null,
                            // Clear child selections
                            selectedIlce = null,
                            selectedDepo = null,
                            availableDepolar = emptyList()
                        )
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Failed to load districts: ${result.message}")
                        handleError("İlçe listesi yüklenemedi: ${result.message}")
                    }
                    is LocationResult.TokenExpired -> {
                        Log.w(TAG, "Token expired while loading districts")
                        handleTokenExpired()
                    }
                }
            }
        }
    }


    private fun loadDepolarByIlce(ilceId: Long) {
        viewModelScope.launch {
            getLocationHierarchyUseCase.getDepolarByIlce(ilceId).collect { result ->
                when (result) {
                    is LocationResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is LocationResult.Success -> {
                        Log.d(TAG, "Warehouses loaded: ${result.data.size} items for district $ilceId")
                        _uiState.value = _uiState.value.copy(
                            availableDepolar = result.data,
                            isLoading = false,
                            errorMessage = null,
                            // Clear child selections
                            selectedDepo = null
                        )
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Failed to load warehouses: ${result.message}")
                        handleError("Depo listesi yüklenemedi: ${result.message}")
                    }
                    is LocationResult.TokenExpired -> {
                        Log.w(TAG, "Token expired while loading warehouses")
                        handleTokenExpired()
                    }
                }
            }
        }
    }


    fun onBolgeSelected(bolge: BolgeDto) {
        Log.d(TAG, "Region selected: ${bolge.bolgeAdi} (ID: ${bolge.id})")

        _uiState.value = _uiState.value.copy(
            selectedBolge = bolge,
            // Clear all child selections
            selectedIl = null,
            selectedIlce = null,
            selectedDepo = null,
            availableIller = emptyList(),
            availableIlceler = emptyList(),
            availableDepolar = emptyList(),
            errorMessage = null
        )

        // Load provinces for selected region
        loadIllerByBolge(bolge.id)
    }


    fun onIlSelected(il: IlDto) {
        Log.d(TAG, "Province selected: ${il.ilAdi} (ID: ${il.id})")

        _uiState.value = _uiState.value.copy(
            selectedIl = il,
            // Clear child selections
            selectedIlce = null,
            selectedDepo = null,
            availableIlceler = emptyList(),
            availableDepolar = emptyList(),
            errorMessage = null
        )

        // Load districts for selected province
        loadIlcelerByIl(il.id)
    }

    fun onIlceSelected(ilce: IlceDto) {
        Log.d(TAG, "District selected: ${ilce.ilceAdi} (ID: ${ilce.id})")

        _uiState.value = _uiState.value.copy(
            selectedIlce = ilce,
            // Clear child selections
            selectedDepo = null,
            availableDepolar = emptyList(),
            errorMessage = null
        )

        // Load warehouses for selected district
        loadDepolarByIlce(ilce.id)
    }

    fun onDepoSelected(depo: DepoDto) {
        Log.d(TAG, "Warehouse selected: ${depo.depoAdi} (ID: ${depo.id})")

        _uiState.value = _uiState.value.copy(
            selectedDepo = depo,
            errorMessage = null
        )

        Log.d(TAG, "Location selection completed: ${_uiState.value.getSelectionSummary()}")
    }

    fun saveLocationSelection() {
        val currentState = _uiState.value

        if (!currentState.isSelectionComplete()) {
            handleError("Lütfen bölge ve depo seçimi yapınız")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving location selection to backend")

                val bolge = currentState.selectedBolge!!
                val depo = currentState.selectedDepo!!

                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                setUserLocationUseCase.execute(bolge.id, depo.id).collect { result ->
                    when (result) {
                        is LocationResult.Loading -> {
                            // Already handled above
                        }
                        is LocationResult.Success -> {
                            Log.d(TAG, "Location saved successfully")
                            _uiState.value = _uiState.value.copy(isLoading = false)

                            // Navigate back with success
                            _navigationEvent.emit(
                                LocationNavigationEvent.NavigateBackWithSuccess(
                                    message = "Lokasyon başarıyla güncellendi",
                                    locationSummary = currentState.getSelectionSummary() ?: ""
                                )
                            )
                        }
                        is LocationResult.Error -> {
                            Log.e(TAG, "Failed to save location: ${result.message}")
                            handleError("Lokasyon kaydedilemedi: ${result.message}")
                        }
                        is LocationResult.TokenExpired -> {
                            Log.w(TAG, "Token expired while saving location")
                            handleTokenExpired()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error saving location", e)
                handleError("Lokasyon kaydetme hatası: ${e.message}")
            }
        }
    }


    fun resetSelection() {
        Log.d(TAG, "Resetting location selection")

        _uiState.value = _uiState.value.copy(
            selectedBolge = null,
            selectedIl = null,
            selectedIlce = null,
            selectedDepo = null,
            availableIller = emptyList(),
            availableIlceler = emptyList(),
            availableDepolar = emptyList(),
            errorMessage = null
        )

        // Reload initial regions
        loadBolgeler()
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }


    fun onBackPressed() {
        viewModelScope.launch {
            Log.d(TAG, "Back navigation requested")
            _navigationEvent.emit(LocationNavigationEvent.NavigateBack)
        }
    }


    fun refreshData() {
        Log.d(TAG, "Refreshing location data")
        resetSelection()
    }


    private fun handleError(message: String) {
        Log.e(TAG, "Error: $message")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    private fun handleTokenExpired() {
        viewModelScope.launch {
            Log.w(TAG, "Token expired - navigating to login")
            _navigationEvent.emit(LocationNavigationEvent.NavigateToLogin)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "LocationViewModel cleared")
    }
}

sealed class LocationNavigationEvent {
    object NavigateBack : LocationNavigationEvent()
    object NavigateToLogin : LocationNavigationEvent()
    data class NavigateBackWithSuccess(
        val message: String,
        val locationSummary: String
    ) : LocationNavigationEvent()
    data class ShowError(val message: String) : LocationNavigationEvent()
}