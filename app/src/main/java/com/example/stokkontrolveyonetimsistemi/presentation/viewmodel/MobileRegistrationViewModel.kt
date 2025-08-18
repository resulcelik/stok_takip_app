package com.example.stokkontrolveyonetimsistemi.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.location.LocationResult
import com.example.stokkontrolveyonetimsistemi.data.model.location.StokBirimiDto
import com.example.stokkontrolveyonetimsistemi.data.model.raf.RafCreateResponse
import com.example.stokkontrolveyonetimsistemi.data.model.registration.*
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserSessionDto
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunBilgileri
import com.example.stokkontrolveyonetimsistemi.data.repository.MobileRegistrationRepository
import com.example.stokkontrolveyonetimsistemi.data.repository.MobileResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Mobile Registration Workflow
 * Ürün ekleme sürecinin tüm adımlarını yönetir
 *
 * DÜZELTILMIŞ WORKFLOW:
 * 1. Lokasyon Kontrolü - Location seçimi kontrol edilir
 * 2. RAF Barkod Okutma - RAF barkodu okutulur ve backend'e kaydedilir
 * 3. Ürün Barkod Okutma - Ürün numarası barkoddan okutulur
 * 4. Ürün Detayları Girişi - Açıklama, stok birimi vs.
 * 5. Fotoğraf Çekimi - Minimum 4 fotoğraf
 * 6. Özet ve Kayıt Tamamlama
 */
class MobileRegistrationViewModel(
    private val repository: MobileRegistrationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MobileRegViewModel"
        private const val MIN_PHOTO_COUNT = 4
        private const val MAX_PHOTO_SIZE = 10 * 1024 * 1024 // 10MB
    }

    // ==========================================
    // STATE MANAGEMENT
    // ==========================================

    // Current workflow step
    private val _currentStep = MutableStateFlow(RegistrationStep.LOCATION_CHECK)
    val currentStep: StateFlow<RegistrationStep> = _currentStep.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Success message
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // ==========================================
    // DATA HOLDERS
    // ==========================================

    // User session & location
    private val _userSession = MutableLiveData<UserSessionDto?>()
    val userSession: LiveData<UserSessionDto?> = _userSession

    // RAF data
    private val _rafData = MutableLiveData<RafCreateResponse?>()
    val rafData: LiveData<RafCreateResponse?> = _rafData

    private val _rafSeriNo = MutableStateFlow("")
    val rafSeriNo: StateFlow<String> = _rafSeriNo.asStateFlow()

    // RAF kaydedildi mi?
    private val _isRafSaved = MutableStateFlow(false)
    val isRafSaved: StateFlow<Boolean> = _isRafSaved.asStateFlow()

    // Ürün data
    private val _urunBilgileri = MutableStateFlow(
        UrunBilgileri(
            tasnifNo = "", // Barkoddan okunacak
            aciklama = "",
            markaId = null,
            stokBirimiId = 0,
            stokBirimi2Id = null,
            en = null,
            boy = null,
            yukseklik = null
        )
    )
    val urunBilgileri: StateFlow<UrunBilgileri> = _urunBilgileri.asStateFlow()

    // Ürün barkodu okutuldu mu?
    private val _isUrunBarcodeScanned = MutableStateFlow(false)
    val isUrunBarcodeScanned: StateFlow<Boolean> = _isUrunBarcodeScanned.asStateFlow()

    // Stok birimleri dropdown
    private val _stokBirimleri = MutableLiveData<List<StokBirimiDto>>()
    val stokBirimleri: LiveData<List<StokBirimiDto>> = _stokBirimleri

    // Photo management - Local file paths
    private val _uploadedPhotos = MutableStateFlow<List<UploadedPhoto>>(emptyList())
    val uploadedPhotos: StateFlow<List<UploadedPhoto>> = _uploadedPhotos.asStateFlow()

    // Created Ürün Seri No (kayıt sonrası)
    private val _createdUrunSeriNo = MutableStateFlow<String?>(null)
    val createdUrunSeriNo: StateFlow<String?> = _createdUrunSeriNo.asStateFlow()

    // ==========================================
    // VALIDATION STATES
    // ==========================================

    val hasValidLocation: Boolean
        get() = _userSession.value?.selectedDepoId != null && _userSession.value?.selectedDepoId!! > 0

    val hasValidRaf: Boolean
        get() = _rafSeriNo.value.isNotEmpty() && _isRafSaved.value

    val hasValidUrunBarcode: Boolean
        get() = _urunBilgileri.value.tasnifNo.isNotEmpty() && _isUrunBarcodeScanned.value

    val hasValidUrunInfo: Boolean
        get() = _urunBilgileri.value.isValid()

    val hasEnoughPhotos: Boolean
        get() = _uploadedPhotos.value.size >= MIN_PHOTO_COUNT

    val canProceedToNext: Boolean
        get() = when (_currentStep.value) {
            RegistrationStep.LOCATION_CHECK -> hasValidLocation
            RegistrationStep.RAF_SCAN -> hasValidRaf
            RegistrationStep.URUN_BARCODE -> hasValidUrunBarcode
            RegistrationStep.URUN_DETAIL -> hasValidUrunInfo
            RegistrationStep.PHOTO_CAPTURE -> hasEnoughPhotos
            RegistrationStep.REVIEW_SUBMIT -> true
        }

    // ==========================================
    // INITIALIZATION
    // ==========================================

    init {
        checkUserSession()
        loadStokBirimleri()
    }

    // ==========================================
    // NAVIGATION
    // ==========================================

    fun navigateToNextStep() {
        when (_currentStep.value) {
            RegistrationStep.LOCATION_CHECK -> {
                if (hasValidLocation) {
                    _currentStep.value = RegistrationStep.RAF_SCAN
                } else {
                    _errorMessage.value = "Lütfen önce lokasyon seçimi yapınız"
                }
            }
            RegistrationStep.RAF_SCAN -> {
                if (hasValidRaf) {
                    _currentStep.value = RegistrationStep.URUN_BARCODE
                } else {
                    _errorMessage.value = "Lütfen önce RAF'ı kaydedin"
                }
            }
            RegistrationStep.URUN_BARCODE -> {
                if (hasValidUrunBarcode) {
                    _currentStep.value = RegistrationStep.URUN_DETAIL
                } else {
                    _errorMessage.value = "Lütfen ürün barkodunu okutun"
                }
            }
            RegistrationStep.URUN_DETAIL -> {
                if (hasValidUrunInfo) {
                    _currentStep.value = RegistrationStep.PHOTO_CAPTURE
                } else {
                    _errorMessage.value = "Lütfen tüm zorunlu alanları doldurun"
                }
            }
            RegistrationStep.PHOTO_CAPTURE -> {
                if (hasEnoughPhotos) {
                    _currentStep.value = RegistrationStep.REVIEW_SUBMIT
                } else {
                    _errorMessage.value = "En az $MIN_PHOTO_COUNT fotoğraf gerekli"
                }
            }
            RegistrationStep.REVIEW_SUBMIT -> {
                // Son adım
            }
        }
    }

    fun navigateToPreviousStep() {
        when (_currentStep.value) {
            RegistrationStep.RAF_SCAN -> _currentStep.value = RegistrationStep.LOCATION_CHECK
            RegistrationStep.URUN_BARCODE -> _currentStep.value = RegistrationStep.RAF_SCAN
            RegistrationStep.URUN_DETAIL -> _currentStep.value = RegistrationStep.URUN_BARCODE
            RegistrationStep.PHOTO_CAPTURE -> _currentStep.value = RegistrationStep.URUN_DETAIL
            RegistrationStep.REVIEW_SUBMIT -> _currentStep.value = RegistrationStep.PHOTO_CAPTURE
            else -> {}
        }
    }

    fun forceEnableUrunBarcode() {
        if (_urunBilgileri.value.tasnifNo.isNotEmpty()) {
            _isUrunBarcodeScanned.value = true
            Log.d(TAG, "Force enabled ürün barcode: ${_urunBilgileri.value.tasnifNo}")
        }
    }

    fun debugLogStates() {
        Log.d(TAG, "=== DEBUG STATE DUMP ===")
        Log.d(TAG, "currentStep: ${_currentStep.value}")
        Log.d(TAG, "rafSeriNo: '${_rafSeriNo.value}'")
        Log.d(TAG, "isRafSaved: ${_isRafSaved.value}")
        Log.d(TAG, "urunBilgileri.tasnifNo: '${_urunBilgileri.value.tasnifNo}'")
        Log.d(TAG, "isUrunBarcodeScanned: ${_isUrunBarcodeScanned.value}")
        Log.d(TAG, "hasValidLocation: $hasValidLocation")
        Log.d(TAG, "hasValidRaf: $hasValidRaf")
        Log.d(TAG, "hasValidUrunBarcode: $hasValidUrunBarcode")
        Log.d(TAG, "canProceedToNext: $canProceedToNext")
        Log.d(TAG, "========================")
    }

    // ==========================================
    // LOCATION & SESSION OPERATIONS
    // ==========================================

    fun checkUserSession() {
        viewModelScope.launch {
            repository.getCurrentSession().collect { result ->
                when (result) {
                    is LocationResult.Loading -> {
                        _isLoading.value = true
                    }
                    is LocationResult.Success -> {
                        _isLoading.value = false
                        _userSession.value = result.data

                        if (!hasValidLocation) {
                            _errorMessage.value = "Lütfen önce lokasyon seçimi yapınız"
                        }

                        Log.d(TAG, "Session loaded: depoId=${result.data.selectedDepoId}")
                    }
                    is LocationResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is LocationResult.TokenExpired -> {
                        _isLoading.value = false
                        _errorMessage.value = "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
                    }
                }
            }
        }
    }

    // ==========================================
    // RAF OPERATIONS
    // ==========================================

    /**
     * RAF barkodu okutulduğunda çağrılır
     */
    fun processRafBarcode(rafNo: String) {
        if (rafNo.isEmpty()) {
            _errorMessage.value = "RAF numarası boş olamaz"
            return
        }

        if (!isValidRafFormat(rafNo)) {
            _errorMessage.value = "Geçersiz RAF formatı! R ile başlamalı ve 12 karakter olmalı."
            return
        }

        _rafSeriNo.value = rafNo
        _isRafSaved.value = false

        Log.d(TAG, "RAF barkodu okundu: $rafNo")
    }

    /**
     * RAF'ı backend'e kaydet
     */
    fun saveRafToBackend() {
        if (_rafSeriNo.value.isEmpty()) {
            _errorMessage.value = "RAF numarası boş olamaz"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.createMobileRaf(_rafSeriNo.value).collect { result ->
                    when (result) {
                        is MobileResult.Loading -> {
                            _isLoading.value = true
                        }
                        is MobileResult.Success -> {
                            _isLoading.value = false
                            _isRafSaved.value = true
                            _rafData.value = result.data
                            // Backend'den data gelmediği için manuel oluştur

                            _successMessage.value = "RAF başarıyla kaydedildi: ${_rafSeriNo.value}"
                            Log.d(TAG, "RAF saved successfully: ${_rafSeriNo.value}")
                        }
                        is MobileResult.Error -> {
                            _isLoading.value = false
                            _isRafSaved.value = false
                            _errorMessage.value = result.message
                        }
                        is MobileResult.ValidationError -> {
                            _isLoading.value = false
                            _isRafSaved.value = false
                            _errorMessage.value = result.message
                        }
                        is MobileResult.TokenExpired -> {
                            _isLoading.value = false
                            _isRafSaved.value = false
                            _errorMessage.value = "Oturum süresi dolmuş"
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _isRafSaved.value = false
                _errorMessage.value = "RAF kaydedilemedi: ${e.message}"
            }
        }
    }

    fun clearRafData() {
        _rafData.value = null
        _rafSeriNo.value = ""
        _isRafSaved.value = false
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun isValidRafFormat(rafNo: String): Boolean {
        return rafNo.startsWith("R") && rafNo.length == 12
    }

    // ==========================================
    // ÜRÜN BARCODE OPERATIONS
    // ==========================================

    /**
     * Ürün barkodu okutulduğunda çağrılır
     */
    fun processUrunBarcode(urunNo: String) {
        if (urunNo.isEmpty()) {
            _errorMessage.value = "Ürün numarası boş olamaz"
            return
        }

        if (!isValidUrunFormat(urunNo)) {
            _errorMessage.value = "Geçersiz ürün formatı! U ile başlamalı ve 12 karakter olmalı."
            return
        }

        // Ürün numarasını set et
        _urunBilgileri.value = _urunBilgileri.value.copy(
            tasnifNo = urunNo
        )
        _isUrunBarcodeScanned.value = true  // ÖNEMLİ: Bu satır eklendi/kontrol edildi

        _successMessage.value = "Ürün barkodu okundu: $urunNo"
        Log.d(TAG, "Ürün barkodu okundu: $urunNo, isScanned: ${_isUrunBarcodeScanned.value}")
    }

    /**
     * Ürün barkodunu temizle (yeniden okutma için)
     */
    fun clearUrunBarcode() {
        _urunBilgileri.value = _urunBilgileri.value.copy(
            tasnifNo = ""
        )
        _isUrunBarcodeScanned.value = false
        _errorMessage.value = null
        _successMessage.value = null
    }

    private fun isValidUrunFormat(urunNo: String): Boolean {
        return urunNo.startsWith("U") && urunNo.length == 12
    }

    // ==========================================
    // ÜRÜN DETAIL OPERATIONS
    // ==========================================

    private fun loadStokBirimleri() {
        viewModelScope.launch {
            repository.getStokBirimleri().collect { result ->
                when (result) {
                    is LocationResult.Success -> {
                        _stokBirimleri.value = result.data
                        Log.d(TAG, "Loaded ${result.data.size} stok birimleri")
                    }
                    is LocationResult.Error -> {
                        Log.e(TAG, "Error loading stok birimleri: ${result.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun updateUrunBilgileri(
        aciklama: String? = null,
        stokBirimiId: Long? = null,
        stokBirimi2Id: Long? = null,
        en: Double? = null,
        boy: Double? = null,
        yukseklik: Double? = null
    ) {
        _urunBilgileri.value = _urunBilgileri.value.copy(
            aciklama = aciklama ?: _urunBilgileri.value.aciklama,
            markaId = null, // Her zaman null
            stokBirimiId = stokBirimiId ?: _urunBilgileri.value.stokBirimiId,
            stokBirimi2Id = stokBirimi2Id,
            en = en,
            boy = boy,
            yukseklik = yukseklik
        )
    }

    // ==========================================
    // PHOTO OPERATIONS
    // ==========================================

    /**
     * Fotoğrafı local olarak sakla (upload kayıttan sonra yapılacak)
     */
    fun addPhoto(photoFile: File) {
        if (!photoFile.exists()) {
            _errorMessage.value = "Fotoğraf dosyası bulunamadı"
            return
        }

        if (photoFile.length() > MAX_PHOTO_SIZE) {
            _errorMessage.value = "Fotoğraf boyutu 10MB'dan büyük olamaz"
            return
        }

        val uploadedPhoto = UploadedPhoto(
            localPath = photoFile.absolutePath,
            fileName = photoFile.name,
            fileSize = photoFile.length(),
            uploadStatus = UploadStatus.PENDING
        )

        val currentPhotos = _uploadedPhotos.value.toMutableList()
        currentPhotos.add(uploadedPhoto)
        _uploadedPhotos.value = currentPhotos

        _successMessage.value = "Fotoğraf eklendi (${currentPhotos.size}/$MIN_PHOTO_COUNT)"
        Log.d(TAG, "Photo added: ${photoFile.name}")
    }

    fun removePhoto(localPath: String) {
        val currentPhotos = _uploadedPhotos.value.toMutableList()
        currentPhotos.removeAll { it.localPath == localPath }
        _uploadedPhotos.value = currentPhotos

        _successMessage.value = "Fotoğraf kaldırıldı"
    }

    private fun updatePhotoStatus(localPath: String, status: UploadStatus) {
        val currentPhotos = _uploadedPhotos.value.toMutableList()
        val index = currentPhotos.indexOfFirst { it.localPath == localPath }

        if (index != -1) {
            currentPhotos[index] = currentPhotos[index].copy(uploadStatus = status)
            _uploadedPhotos.value = currentPhotos
        }
    }

    // ==========================================
    // REGISTRATION COMPLETION
    // ==========================================

    /**
     * Tüm kayıt işlemini tamamla
     * 1. Ürünü backend'e kaydet
     * 2. Fotoğrafları yükle
     */
    fun completeRegistration() {
        if (!validateAllData()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                // 1. Ürünü oluştur
                val urunRequest = UrunCreateRequest(
                    urunSeriNo = _urunBilgileri.value.tasnifNo,
                    rafSeriNo = _rafSeriNo.value,
                    aciklama = _urunBilgileri.value.aciklama,
                    stokBirimiId = _urunBilgileri.value.stokBirimiId,
                    stokBirimi2Id = _urunBilgileri.value.stokBirimi2Id,
                    rafId = _rafData.value?.rafId,
                    depoId = _userSession.value?.selectedDepoId ?: 0L,
                    bolgeId = _userSession.value?.selectedBolgeId ?: 0L,
                    en = _urunBilgileri.value.en,
                    boy = _urunBilgileri.value.boy,
                    yukseklik = _urunBilgileri.value.yukseklik
                )

                repository.createUrun(urunRequest).collect { result ->
                    when (result) {
                        is MobileResult.Success -> {
                            val createdUrunSeriNo = result.data.urunSeriNo
                            _createdUrunSeriNo.value = createdUrunSeriNo


                            Log.d(TAG, "Ürün created: SeriNo=$createdUrunSeriNo")

                            // 2. Fotoğrafları yükle
                            uploadPhotosToBackend(result.data.id)
                        }
                        is MobileResult.Error -> {
                            _isLoading.value = false
                            _errorMessage.value = "Ürün oluşturulamadı: ${result.message}"
                        }
                        is MobileResult.ValidationError -> {
                            _isLoading.value = false
                            _errorMessage.value = result.message
                        }
                        is MobileResult.TokenExpired -> {
                            _isLoading.value = false
                            _errorMessage.value = "Oturum süresi dolmuş"
                        }
                        else -> {}
                    }
                }

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Kayıt başarısız: ${e.message}"
                Log.e(TAG, "Registration failed", e)
            }
        }
    }

    /**
     * Ürün oluşturulduktan sonra fotoğrafları yükle
     */
    private fun uploadPhotosToBackend(id: Long) {
        viewModelScope.launch {
            var successCount = 0
            var failCount = 0

            _uploadedPhotos.value.forEach { photo ->
                try {
                    val file = File(photo.localPath)
                    if (file.exists()) {
                        repository.uploadPhotoForUrun(id, file).collect { result ->
                            when (result) {
                                is MobileResult.Success -> {
                                    successCount++
                                    updatePhotoStatus(photo.localPath, UploadStatus.SUCCESS)
                                    Log.d(TAG, "Photo uploaded: ${photo.fileName}")
                                }
                                is MobileResult.Error -> {
                                    failCount++
                                    updatePhotoStatus(photo.localPath, UploadStatus.FAILED)
                                    Log.e(TAG, "Photo upload failed: ${photo.fileName} - ${result.message}")
                                }
                                else -> {}
                            }
                        }
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.e(TAG, "Photo upload exception: ${photo.fileName}", e)
                }
            }

            _isLoading.value = false

            if (successCount > 0) {
                _successMessage.value = "Kayıt tamamlandı!\n" +
                        "Ürün No: $id\n" +
                        "Fotoğraflar: $successCount başarılı" +
                        if (failCount > 0) ", $failCount hatalı" else ""

                Log.d(TAG, "Registration completed: Ürün=$id, Photos=$successCount/$failCount")
            } else {
                _errorMessage.value = "Fotoğraflar yüklenemedi"
            }
        }
    }

    /**
     * Tüm verileri validate et
     */
    private fun validateAllData(): Boolean {
        return when {
            !hasValidLocation -> {
                _errorMessage.value = "Lokasyon seçimi yapılmamış"
                false
            }
            !hasValidRaf -> {
                _errorMessage.value = "RAF bilgisi eksik veya kaydedilmemiş"
                false
            }
            !hasValidUrunBarcode -> {
                _errorMessage.value = "Ürün barkodu okutulmamış"
                false
            }
            !hasValidUrunInfo -> {
                _errorMessage.value = "Ürün bilgileri eksik veya hatalı"
                false
            }
            !hasEnoughPhotos -> {
                _errorMessage.value = "En az $MIN_PHOTO_COUNT fotoğraf gerekli"
                false
            }
            else -> true
        }
    }

    // ==========================================
    // WORKFLOW RESET
    // ==========================================

    /**
     * Tüm workflow'u sıfırla (yeni kayıt için)
     */
    fun resetWorkflow() {
        _currentStep.value = RegistrationStep.LOCATION_CHECK

        // RAF verileri
        _rafData.value = null
        _rafSeriNo.value = ""
        _isRafSaved.value = false

        // Ürün verileri
        _urunBilgileri.value = UrunBilgileri(
            tasnifNo = "",
            aciklama = "",
            markaId = null,
            stokBirimiId = 0,
            stokBirimi2Id = null,
            en = null,
            boy = null,
            yukseklik = null
        )
        _isUrunBarcodeScanned.value = false

        // Fotoğraflar
        _uploadedPhotos.value = emptyList()

        // Sonuçlar
        _createdUrunSeriNo.value = null

        // Mesajlar
        _errorMessage.value = null
        _successMessage.value = null

        Log.d(TAG, "Workflow reset completed")

        // Yeni kayıt için session kontrolü
        checkUserSession()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}

// Data classes for photo management
data class UploadedPhoto(
    val localPath: String,
    val fileName: String,
    val fileSize: Long,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    FAILED
}

// Registration Steps
enum class RegistrationStep(val title: String) {
    LOCATION_CHECK("Lokasyon Kontrolü"),
    RAF_SCAN("RAF Barkod"),
    URUN_BARCODE("Ürün Barkod"),
    URUN_DETAIL("Ürün Detayları"),
    PHOTO_CAPTURE("Fotoğraf Çekimi"),
    REVIEW_SUBMIT("Özet ve Kayıt")
}

// Request model for creating Ürün
data class UrunCreateRequest(
    val urunSeriNo: String,
    val rafSeriNo: String,
    val aciklama: String,
    val bolgeId: Long,  // EKLE
    val depoId: Long,   // EKLE
    val rafId: Long?,
    val stokBirimiId: Long,
    val stokBirimi2Id: Long? = null,
    val en: Double? = null,
    val boy: Double? = null,
    val yukseklik: Double? = null

)