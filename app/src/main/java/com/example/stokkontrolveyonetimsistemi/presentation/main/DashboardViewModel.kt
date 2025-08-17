package com.example.stokkontrolveyonetimsistemi.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.*
import com.example.stokkontrolveyonetimsistemi.data.model.location.LocationResult
import com.example.stokkontrolveyonetimsistemi.data.repository.UserRepository
import com.example.stokkontrolveyonetimsistemi.domain.main.GetUserInfoUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.GetQuickStatsUseCase
import com.example.stokkontrolveyonetimsistemi.domain.main.SessionValidationUseCase
import com.example.stokkontrolveyonetimsistemi.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Enhanced Dashboard ViewModel with User Location Support
 * ✅ UPDATED: UserRepository eklendi, lokasyon yükleme metodu eklendi
 */
class DashboardViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getQuickStatsUseCase: GetQuickStatsUseCase,
    private val sessionValidationUseCase: SessionValidationUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository // ✅ NEW: UserRepository eklendi
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    // ==========================================
    // UI STATE MANAGEMENT
    // ==========================================

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // ==========================================
    // SESSION MONITORING
    // ==========================================

    private var sessionMonitoringJob: Job? = null

    // ==========================================
    // INITIALIZATION
    // ==========================================

    init {
        Log.d(TAG, "Dashboard ViewModel initialized")
        initializeDashboard()
    }

    /**
     * Initialize dashboard data
     * ✅ UPDATED: loadUserSession eklendi
     */
    private fun initializeDashboard() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing dashboard")

                // Start loading
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load data sequentially to avoid conflicts
                loadUserSession() // ✅ NEW: Backend'den user session yükle
                loadQuickStatistics()
                validateSession()

            } catch (e: Exception) {
                Log.e(TAG, "Dashboard initialization failed", e)
                handleError("Dashboard yüklenemedi: ${e.message}")
            }
        }
    }

    // ==========================================
    // DATA LOADING OPERATIONS
    // ==========================================

    /**
     * ✅ NEW: Load user session from backend
     * Backend'den kullanıcı session bilgilerini ve lokasyonu yükle
     */
    private fun loadUserSession() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading user session from backend")

                userRepository.getCurrentUserSession().collect { result ->
                    when (result) {
                        is LocationResult.Loading -> {
                            Log.d(TAG, "Loading user session...")
                        }

                        is LocationResult.Success -> {
                            val sessionDto = result.data
                            Log.d(TAG, "User session loaded: ${sessionDto.username}")
                            Log.d(TAG, "User location: ${sessionDto.getLocationDisplayText()}")

                            // Map DTO to UI model
                            val userSession = UserSession(
                                userId = sessionDto.id,
                                username = sessionDto.username,
                                fullName = sessionDto.getFullName(),
                                email = sessionDto.email,
                                role = mapRole(sessionDto.role),
                                permissions = emptyList(),
                                loginTime = Date(),
                                lastActivity = Date(),
                                sessionId = "",
                                currentLocation = if (sessionDto.hasLocation()) {
                                    UserLocation(
                                        regionId = sessionDto.selectedBolgeId ?: 0L,
                                        regionName = sessionDto.selectedBolgeAdi ?: "",
                                        provinceId = 0L, // Backend'de yok
                                        provinceName = "",
                                        districtId = 0L, // Backend'de yok
                                        districtName = "",
                                        warehouseId = sessionDto.selectedDepoId ?: 0L,
                                        warehouseName = sessionDto.selectedDepoAdi ?: ""
                                    )
                                } else null,
                                scanSoundEnabled = true,
                                scanVibrationEnabled = true,
                                sessionStats = SessionStatistics()
                            )

                            _uiState.value = _uiState.value.copy(
                                userInfo = userSession,
                                isUserLoaded = true,
                                isLoading = false
                            )
                        }

                        is LocationResult.Error -> {
                            Log.e(TAG, "Failed to load user session: ${result.message}")
                            // Fallback to local user info
                            loadUserInformation()
                        }

                        is LocationResult.TokenExpired -> {
                            Log.w(TAG, "Token expired while loading user session")
                            handleSessionExpired()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading user session", e)
                // Fallback to local user info
                loadUserInformation()
            }
        }
    }

    /**
     * Load user information from local storage (fallback)
     * ✅ UPDATED: Artık fallback olarak kullanılıyor
     */
    private suspend fun loadUserInformation() {
        try {
            Log.d(TAG, "Loading user information from local storage (fallback)")

            getUserInfoUseCase.execute().collect { result ->
                result.fold(
                    onSuccess = { userSession ->
                        Log.d(TAG, "User information loaded from local: ${userSession.getDisplayName()}")
                        _uiState.value = _uiState.value.copy(
                            userInfo = userSession,
                            isUserLoaded = true,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        Log.w(TAG, "Failed to load user information from local", exception)
                        _uiState.value = _uiState.value.copy(
                            isUserLoaded = true,
                            isLoading = false
                        )
                        handleError("Kullanıcı bilgileri yüklenemedi")
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load user information error", e)
            handleError("Kullanıcı bilgisi hatası")
        }
    }

    private suspend fun loadQuickStatistics(forceRefresh: Boolean = false) {
        try {
            Log.d(TAG, "Loading quick statistics (forceRefresh: $forceRefresh)")

            getQuickStatsUseCase.execute(forceRefresh).collect { result ->
                result.fold(
                    onSuccess = { stats ->
                        Log.d(TAG, "Quick statistics loaded")
                        _uiState.value = _uiState.value.copy(
                            quickStats = stats,
                            isStatsLoaded = true,
                            isRefreshing = false
                        )
                    },
                    onFailure = { exception ->
                        Log.w(TAG, "Failed to load quick statistics", exception)
                        _uiState.value = _uiState.value.copy(
                            isStatsLoaded = true,
                            isRefreshing = false
                        )
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load quick statistics error", e)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private suspend fun validateSession() {
        try {
            Log.d(TAG, "Validating user session")

            sessionValidationUseCase.execute().collect { validationState ->
                val sessionInfo = sessionValidationUseCase.getSessionInfo()

                _uiState.value = _uiState.value.copy(
                    sessionState = validationState,
                    tokenExpiryMinutes = sessionInfo.minutesUntilExpiry
                )

                when (validationState) {
                    is SessionValidationState.Expired -> {
                        Log.w(TAG, "Session expired - initiating logout")
                        handleSessionExpired()
                    }
                    is SessionValidationState.Warning -> {
                        Log.w(TAG, "Session warning: ${validationState.minutesLeft} minutes left")
                    }
                    is SessionValidationState.Error -> {
                        Log.e(TAG, "Session validation error: ${validationState.message}")
                        if (!validationState.message.contains("network", ignoreCase = true)) {
                            handleError("Oturum doğrulama hatası")
                        }
                    }
                    else -> {
                        Log.d(TAG, "Session valid")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session validation error", e)
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Map role string to UserRole enum
     */
    private fun mapRole(roleString: String?): UserRole {
        return when (roleString?.uppercase()) {
            "ADMIN" -> UserRole.ADMIN
            "MANAGER" -> UserRole.MANAGER
            "SUPERVISOR" -> UserRole.SUPERVISOR
            else -> UserRole.USER
        }
    }

    // ==========================================
    // SESSION MONITORING (SAFE VERSION)
    // ==========================================

    private suspend fun startSessionMonitoring() {
        try {
            Log.d(TAG, "Starting session monitoring")
            validateSession()
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "Session monitoring cancelled (normal)")
        } catch (e: Exception) {
            Log.e(TAG, "Session monitoring error", e)
        }
    }

    // ==========================================
    // ACTION HANDLING
    // ==========================================

    fun handleAction(action: DashboardAction) {
        viewModelScope.launch {
            Log.d(TAG, "Handling action: $action")

            when (action) {
                is DashboardAction.RefreshData -> refreshAllData()
                is DashboardAction.LoadUserInfo -> loadUserSession() // ✅ UPDATED
                is DashboardAction.LoadStats -> loadQuickStatistics()
                is DashboardAction.ValidateSession -> validateSession()

                // UI State Actions
                is DashboardAction.ShowNotifications -> showNotifications()
                is DashboardAction.HideNotifications -> hideNotifications()
                is DashboardAction.ShowUserMenu -> showUserMenu()
                is DashboardAction.HideUserMenu -> hideUserMenu()

                // Navigation Actions
                is DashboardAction.NavigateToModule -> navigateToModule(action.module)

                // Business Actions
                is DashboardAction.GenerateRafNumarasi -> generateRafNumarasi()
                is DashboardAction.GenerateUrunNumarasi -> generateUrunNumarasi()
                is DashboardAction.AddNewProduct -> addNewProduct()
                is DashboardAction.StartBarcodeScanning -> startBarcodeScanning()

                // Settings Actions
                is DashboardAction.ChangePassword -> changePassword()
                is DashboardAction.ChangeLocation -> changeLocation()

                // Error Actions
                is DashboardAction.ShowError -> showError(action.message)
                is DashboardAction.ClearError -> clearError()

                // Auth Actions
                is DashboardAction.Logout -> performLogout()
            }
        }
    }

    // ==========================================
    // NAVIGATION ACTIONS
    // ==========================================

    private suspend fun navigateToModule(module: DashboardModule) {
        try {
            Log.d(TAG, "Navigating to module: ${module.title}")

            _uiState.value = _uiState.value.copy(selectedModule = module)
            sessionValidationUseCase.updateLastActivity()
            _navigationEvent.emit(NavigationEvent.NavigateToModule(module))

        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed", e)
            handleError("Navigasyon hatası")
        }
    }

    // ==========================================
    // BUSINESS ACTIONS (PLACEHOLDERS)
    // ==========================================

    private fun generateRafNumarasi() {
        Log.d(TAG, "RAF_URETICI action - placeholder")
        showError("Raf üretim modülü geliştiriliyor")
    }

    private fun generateUrunNumarasi() {
        Log.d(TAG, "URUN_URETICI action - placeholder")
        showError("Ürün numara üretim modülü geliştiriliyor")
    }

    private fun addNewProduct() {
        Log.d(TAG, "URUN_EKLE action - placeholder")
        showError("Ürün ekleme modülü geliştiriliyor")
    }

    private fun startBarcodeScanning() {
        Log.d(TAG, "SCANNER action - placeholder")
        showError("Barkod tarayıcı modülü geliştiriliyor")
    }

    private fun changePassword() {
        Log.d(TAG, "PASSWORD_CHANGE action - will navigate to settings")
        viewModelScope.launch {
            navigateToModule(DashboardModule.SETTINGS)
        }
    }

    private fun changeLocation() {
        Log.d(TAG, "LOCATION_CHANGE action - will navigate to settings")
        viewModelScope.launch {
            navigateToModule(DashboardModule.SETTINGS)
        }
    }

    // ==========================================
    // DATA REFRESH ACTIONS
    // ==========================================

    /**
     * ✅ UPDATED: loadUserSession eklendi
     */
    private suspend fun refreshAllData() {
        try {
            Log.d(TAG, "Refreshing all dashboard data")
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            loadUserSession() // ✅ NEW: Backend'den user session yükle
            loadQuickStatistics(forceRefresh = true)
            validateSession()

            sessionValidationUseCase.updateLastActivity()

        } catch (e: Exception) {
            Log.e(TAG, "Refresh all data failed", e)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
            handleError("Yenileme başarısız")
        }
    }

    // ==========================================
    // UI STATE ACTIONS
    // ==========================================

    private fun showNotifications() {
        _uiState.value = _uiState.value.copy(showNotifications = true)
    }

    private fun hideNotifications() {
        _uiState.value = _uiState.value.copy(showNotifications = false)
    }

    private fun showUserMenu() {
        _uiState.value = _uiState.value.copy(showUserMenu = true)
    }

    private fun hideUserMenu() {
        _uiState.value = _uiState.value.copy(showUserMenu = false)
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            showError = true
        )
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showError = false
        )
    }

    private fun handleError(message: String) {
        Log.e(TAG, "Handling error: $message")
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            showError = true,
            isLoading = false,
            isRefreshing = false
        )
    }

    private suspend fun handleSessionExpired() {
        Log.w(TAG, "Handling session expiry")
        _navigationEvent.emit(
            NavigationEvent.NavigateToLogin("Oturum süresi doldu, lütfen tekrar giriş yapın")
        )
    }

    // ==========================================
    // LOGOUT HANDLING
    // ==========================================

    private fun performLogout() {
        Log.d(TAG, "Logout initiated")

        viewModelScope.launch {
            try {
                logoutUseCase.execute()
                _navigationEvent.emit(NavigationEvent.NavigateToLogin("Çıkış başarılı"))

            } catch (e: Exception) {
                Log.e(TAG, "Logout failed", e)
                _navigationEvent.emit(NavigationEvent.NavigateToLogin("Çıkış yapıldı"))
            }
        }
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Dashboard ViewModel cleared")

        try {
            sessionMonitoringJob?.cancel()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    fun onResume() {
        viewModelScope.launch {
            Log.d(TAG, "Dashboard resumed - validating session")
            loadUserSession() // ✅ NEW: Resume'de lokasyonu yeniden yükle
            validateSession()
            sessionValidationUseCase.updateLastActivity()
        }
    }

    fun onPause() {
        Log.d(TAG, "Dashboard paused")
        sessionValidationUseCase.updateLastActivity()
    }
}

/**
 * Enhanced Navigation events for UI
 */
sealed class NavigationEvent {
    data class NavigateToModule(val module: DashboardModule) : NavigationEvent()
    data class NavigateToLogin(val message: String? = null) : NavigationEvent()
    data class ShowError(val message: String) : NavigationEvent()
    data class ShowMessage(val message: String) : NavigationEvent()
    object ShowSessionWarning : NavigationEvent()
}