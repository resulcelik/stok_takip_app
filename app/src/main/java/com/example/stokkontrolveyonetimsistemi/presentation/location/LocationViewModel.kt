package com.example.stokkontrolveyonetimsistemi.presentation.location

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.GetLocationHierarchyUseCase
import com.example.stokkontrolveyonetimsistemi.domain.usecase.location.SetUserLocationUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Location Selection ViewModel
 * 4-seviye cascade dropdown yönetimi için UI state management
 *
 * Dosya Konumu: /presentation/location/LocationViewModel.kt
 * Pattern: DashboardViewModel.kt ve SettingsViewModel.kt pattern'ını takip ediyor
 * Features: Cascade dropdown logic, real-time API calls, error handling
 */
class LocationViewModel(
    private val getLocationHierarchyUseCase: GetLocationHierarchyUseCase,
    private val setUserLocationUseCase: SetUserLocationUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LocationViewModel"
    }

    // ==========================================
    // UI STATE MANAGEMENT
    // ==========================================

    private val _uiState = MutableStateFlow(LocationSelectionState())
    val uiState: StateFlow<LocationSelectionState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LocationNavigationEvent>()
    val navigationEvent: SharedFlow<LocationNavigationEvent> = _navigationEvent.asSharedFlow()

    // ==========================================
    // INITIALIZATION
    // ==========================================

    init {
        Log.d(TAG, "LocationViewModel initialized")
        loadInitialData()
    }

    /**
     * Load initial regions data
     * Ekran açıldığında bölge listesini yükle
     */
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

    // ==========================================
    // CASCADE DROPDOWN OPERATIONS
    // ==========================================

    /**
     * Load all regions (1st level)
     * İlk dropdown seviyesi - tüm bölgeleri getir
     */
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

    /**
     * Load provinces by region (2nd level)
     * Bölge seçildiğinde il listesini yükle
     */
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

    /**
     * Load districts by province (3rd level)
     * İl seçildiğinde ilçe listesini yükle
     */
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

    /**
     * Load warehouses by district (4th level)
     * İlçe seçildiğinde depo listesini yükle
     */
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

    // ==========================================
    // USER SELECTION ACTIONS
    // ==========================================

    /**
     * Handle region selection (1st level)
     * Bölge seçildiğinde tetiklenir
     */
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

    /**
     * Handle province selection (2nd level)
     * İl seçildiğinde tetiklenir
     */
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

    /**
     * Handle district selection (3rd level)
     * İlçe seçildiğinde tetiklenir
     */
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

    /**
     * Handle warehouse selection (4th level - final)
     * Depo seçildiğinde tetiklenir
     */
    fun onDepoSelected(depo: DepoDto) {
        Log.d(TAG, "Warehouse selected: ${depo.depoAdi} (ID: ${depo.id})")

        _uiState.value = _uiState.value.copy(
            selectedDepo = depo,
            errorMessage = null
        )

        Log.d(TAG, "Location selection completed: ${_uiState.value.getSelectionSummary()}")
    }

    // ==========================================
    // LOCATION SETTING OPERATIONS
    // ==========================================

    /**
     * Save selected location to user session
     * Seçilen lokasyonu backend'e kaydet
     */
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

    // ==========================================
    // RESET OPERATIONS
    // ==========================================

    /**
     * Reset location selection (start over)
     * Tüm seçimleri sıfırla
     */
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

    /**
     * Clear error message
     * Hata mesajını temizle
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ==========================================
    // NAVIGATION ACTIONS
    // ==========================================

    /**
     * Handle back navigation
     * Geri butonuna basıldığında
     */
    fun onBackPressed() {
        viewModelScope.launch {
            Log.d(TAG, "Back navigation requested")
            _navigationEvent.emit(LocationNavigationEvent.NavigateBack)
        }
    }

    /**
     * Refresh data
     * Pull-to-refresh için
     */
    fun refreshData() {
        Log.d(TAG, "Refreshing location data")
        resetSelection()
    }

    // ==========================================
    // ERROR HANDLING
    // ==========================================

    /**
     * Handle general errors
     */
    private fun handleError(message: String) {
        Log.e(TAG, "Error: $message")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    /**
     * Handle token expiration
     */
    private fun handleTokenExpired() {
        viewModelScope.launch {
            Log.w(TAG, "Token expired - navigating to login")
            _navigationEvent.emit(LocationNavigationEvent.NavigateToLogin)
        }
    }

    // ==========================================
    // VALIDATION HELPERS
    // ==========================================

    /**
     * Validate current selection state
     * Mevcut seçimlerin geçerliliğini kontrol et
     */
    fun validateCurrentSelection(): String? {
        val state = _uiState.value

        return when {
            state.selectedBolge == null -> "Lütfen bölge seçiniz"
            state.selectedDepo == null -> "Lütfen depo seçiniz"
            else -> null // Valid selection
        }
    }

    /**
     * Get selection progress (0.0 to 1.0)
     * Seçim ilerleme durumu
     */
    fun getSelectionProgress(): Float {
        val state = _uiState.value
        var progress = 0f

        if (state.selectedBolge != null) progress += 0.25f
        if (state.selectedIl != null) progress += 0.25f
        if (state.selectedIlce != null) progress += 0.25f
        if (state.selectedDepo != null) progress += 0.25f

        return progress
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "LocationViewModel cleared")
    }
}

// ==========================================
// NAVIGATION EVENTS
// ==========================================

/**
 * Location navigation events
 * UI navigation için event system
 */
sealed class LocationNavigationEvent {
    object NavigateBack : LocationNavigationEvent()
    object NavigateToLogin : LocationNavigationEvent()
    data class NavigateBackWithSuccess(
        val message: String,
        val locationSummary: String
    ) : LocationNavigationEvent()
    data class ShowError(val message: String) : LocationNavigationEvent()
}