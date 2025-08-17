package com.example.stokkontrolveyonetimsistemi.presentation.urun

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.product.*
import com.example.stokkontrolveyonetimsistemi.data.repository.UrunEtiketRepository
import com.example.stokkontrolveyonetimsistemi.printer.TscAlpha3RPrinterService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ÜRÜN Etiket ViewModel - Temiz ve Optimize
 * Bluetooth printer desteği ile ÜRÜN etiketi üretimi ve yazdırma
 */
class UrunEtiketViewModel(
    private val repository: UrunEtiketRepository,
    private val printerService: TscAlpha3RPrinterService
) : ViewModel() {

    companion object {
        private const val TAG = "UrunEtiketViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(UrunEtiketUiState())
    val uiState: StateFlow<UrunEtiketUiState> = _uiState.asStateFlow()

    // Printer State
    private val _printerState = MutableStateFlow(PrinterState())
    val printerState: StateFlow<PrinterState> = _printerState.asStateFlow()

    // Events
    private val _events = MutableSharedFlow<UrunEtiketEvent>()
    val events: SharedFlow<UrunEtiketEvent> = _events.asSharedFlow()

    init {
        Log.d(TAG, "ViewModel initialized")
        checkPrinterConnection()
    }

    /**
     * Printer bağlantısını kontrol et
     */
    private fun checkPrinterConnection() {
        viewModelScope.launch {
            try {
                val tscPrinter = printerService.findPairedTscPrinter()
                if (tscPrinter != null) {
                    _printerState.update {
                        it.copy(
                            isConnected = true,
                            printerName = tscPrinter.name
                        )
                    }
                    Log.d(TAG, "Printer bulundu: ${tscPrinter.name}")
                } else {
                    Log.w(TAG, "TSC Printer bulunamadı")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Printer kontrol hatası", e)
            }
        }
    }

    /**
     * ÜRÜN etiketi üret ve otomatik yazdır
     */
    fun generateUrunEtiketleri(adet: Int) {
        // Validasyon
        if (adet !in 1..1000) {
            viewModelScope.launch {
                _events.emit(UrunEtiketEvent.Error("Adet 1-1000 arasında olmalıdır"))
            }
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "ÜRÜN etiketi üretimi başlatılıyor: $adet adet")

            repository.generateUrunEtiketleri(adet).collect { result ->
                when (result) {
                    is UrunEtiketResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                        Log.d(TAG, "Üretim başladı...")
                    }

                    is UrunEtiketResult.Success -> {
                        handleGenerationSuccess(result.data)
                    }

                    is UrunEtiketResult.Error -> {
                        handleGenerationError(result.message)
                    }
                }
            }
        }
    }

    /**
     * Üretim başarılı - UI güncelle ve yazdır
     */
    private suspend fun handleGenerationSuccess(response: UrunBarkodResponse) {
        try {
            val urunNumber = response.getPrintNumber()  // TEK NUMARA
            val printCount = response.getPrintCount()   // KAÇ ADET

            Log.d(TAG, "Üretim başarılı: $printCount adet")
            Log.d(TAG, "ÜRÜN numarası: $urunNumber")

            // UI State güncelle
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    lastGeneratedNumbers = List(printCount) { urunNumber },
                    lastPrintNumber = urunNumber,  // lastNumber yerine lastPrintNumber
                    printCount = printCount,
                    totalGenerated = state.totalGenerated + printCount,
                    todayGenerated = state.todayGenerated + printCount,
                    error = null
                )
            }

            // Success event - düzeltilmiş parametreler
            _events.emit(UrunEtiketEvent.GenerationSuccess(urunNumber, printCount))

            // Otomatik yazdır
            if (_printerState.value.isConnected) {
                printUrunLabels(urunNumber, printCount)  // response yerine direkt değerler
            } else {
                Log.w(TAG, "Printer bağlı değil")
                _events.emit(UrunEtiketEvent.PrintError("Printer bağlı değil"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Üretim sonrası hata", e)
            _events.emit(UrunEtiketEvent.Error("İşlem hatası: ${e.message}"))
        }
    }

    /**
     * Üretim hatası
     */
    private suspend fun handleGenerationError(message: String) {
        Log.e(TAG, "Üretim hatası: $message")

        _uiState.update {
            it.copy(
                isLoading = false,
                error = message
            )
        }

        _events.emit(UrunEtiketEvent.Error(message))
    }

    /**
     * ÜRÜN etiketlerini yazdır
     */
    private suspend fun printUrunLabels(urunNumber: String, printCount: Int) {
        try {
            Log.d(TAG, "Yazdırma başlatılıyor: $urunNumber x $printCount")

            _printerState.update {
                it.copy(
                    isPrinting = true,
                    printedCount = 0,
                    totalCount = printCount
                )
            }

            val tscPrinter = printerService.findPairedTscPrinter()
            if (tscPrinter == null) {
                _events.emit(UrunEtiketEvent.PrintError("TSC Printer bulunamadı"))
                _printerState.update { it.copy(isPrinting = false) }
                return
            }

            var successCount = 0

            // AYNI NUMARAYI İSTENEN ADET KADAR YAZDIR
            for (i in 1..printCount) {
                Log.d(TAG, "Yazdırılıyor: $urunNumber (${i}/$printCount)")

                val success = printerService.printUrunEtiketBluetooth(
                    device = tscPrinter,
                    urunNumber = urunNumber,  // HEP AYNI NUMARA
                    currentIndex = i,
                    totalCount = printCount,
                    useBigText = true
                )

                if (success) {
                    successCount++
                    _printerState.update {
                        it.copy(printedCount = successCount)
                    }

                    _events.emit(
                        UrunEtiketEvent.PrintProgress(successCount, printCount)
                    )
                } else {
                    Log.e(TAG, "Yazdırma hatası: $urunNumber")
                }
            }

            _printerState.update { it.copy(isPrinting = false) }

            if (successCount == printCount) {
                _events.emit(UrunEtiketEvent.PrintSuccess(successCount))
            } else if (successCount > 0) {
                _events.emit(UrunEtiketEvent.PrintError("$successCount/$printCount yazdırıldı"))
            } else {
                _events.emit(UrunEtiketEvent.PrintError("Hiçbir etiket yazdırılamadı"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Yazdırma hatası", e)
            _printerState.update { it.copy(isPrinting = false) }
            _events.emit(UrunEtiketEvent.PrintError(e.message ?: "Yazdırma hatası"))
        }
    }

    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Printer bağlantısını yenile
     */
    fun refreshPrinterConnection() {
        checkPrinterConnection()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}