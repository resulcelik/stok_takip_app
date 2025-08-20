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
 * ÜRÜN Etiket ViewModel - RAF ile AYNI MANTIK
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
                // Önce TSC printer ara
                val tscPrinter = printerService.findPairedTscPrinter()

                if (tscPrinter != null) {
                    _printerState.update {
                        it.copy(
                            isConnected = true,
                            printerName = tscPrinter.name
                        )
                    }
                    Log.d(TAG, "TSC Printer bulundu: ${tscPrinter.name}")
                } else {
                    // TSC yoksa herhangi bir cihaz var mı kontrol et
                    val pairedDevices = printerService.getPairedDevices()
                    if (pairedDevices.isNotEmpty()) {
                        val device = pairedDevices.first()
                        _printerState.update {
                            it.copy(
                                isConnected = true,
                                printerName = device.name
                            )
                        }
                        Log.d(TAG, "Alternatif cihaz bulundu: ${device.name}")
                    } else {
                        Log.w(TAG, "Hiç eşleştirilmiş cihaz yok")
                    }
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
            val urunNumber = response.getPrintNumber()
            val printCount = response.getPrintCount()

            Log.d(TAG, "Üretim başarılı: $printCount adet")
            Log.d(TAG, "ÜRÜN numarası: $urunNumber")

            // UI State güncelle
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    lastGeneratedNumbers = List(printCount) { urunNumber },
                    lastPrintNumber = urunNumber,
                    printCount = printCount,
                    totalGenerated = state.totalGenerated + printCount,
                    todayGenerated = state.todayGenerated + printCount,
                    error = null
                )
            }

            // Success event
            _events.emit(UrunEtiketEvent.GenerationSuccess(urunNumber, printCount))

            // Otomatik yazdır - RAF ile AYNI MANTIK
            printWithBluetooth()

        } catch (e: Exception) {
            Log.e(TAG, "Üretim sonrası hata", e)
            _events.emit(UrunEtiketEvent.Error("İşlem hatası: ${e.message}"))
        }
    }

    /**
     * Bluetooth ile yazdır - RAF ile AYNI MANTIK
     */
    private fun printWithBluetooth() {
        val urunNumbers = _uiState.value.lastGeneratedNumbers
        val urunNumber = _uiState.value.lastPrintNumber
        val printCount = _uiState.value.printCount

        if (urunNumbers.isEmpty() || urunNumber == null) {
            viewModelScope.launch {
                _events.emit(UrunEtiketEvent.PrintError("Yazdırılacak etiket yok"))
            }
            return
        }

        // Bluetooth kontrolü
        if (!printerService.isBluetoothAvailable()) {
            viewModelScope.launch {
                _events.emit(UrunEtiketEvent.PrintError("Bluetooth kapalı veya kullanılamıyor"))
            }
            return
        }

        if (!printerService.hasBluetoothPermissions()) {
            viewModelScope.launch {
                _events.emit(UrunEtiketEvent.PrintError("Bluetooth izinleri gerekli"))
            }
            return
        }

        viewModelScope.launch {
            // TSC veya SPP printer bul - RAF İLE AYNI MANTIK!
            val printer = printerService.findPairedTscPrinter()

            if (printer == null) {
                // Hiç cihaz yoksa hata
                val pairedDevices = printerService.getPairedDevices()
                if (pairedDevices.isEmpty()) {
                    _events.emit(UrunEtiketEvent.PrintError("Eşleştirilmiş Bluetooth cihaz yok"))
                    return@launch
                } else {
                    // Cihaz var ama TSC değil - İLKİNİ KULLAN (RAF GİBİ!)
                    Log.w(TAG, "TSC bulunamadı, ${pairedDevices.size} cihaz var")

                    pairedDevices.firstOrNull()?.let { device ->
                        Log.d(TAG, "Alternatif cihaz kullanılıyor: ${device.name}")
                        _printerState.update {
                            it.copy(isPrinting = true, printedCount = 0, totalCount = printCount)
                        }
                        printToDevice(device, urunNumber, printCount)
                    }
                }
                return@launch
            }

            // TSC Printer bulundu - yazdırmaya başla
            Log.d(TAG, "Printer bulundu: ${printer.name} - yazdırma başlıyor")
            _printerState.update {
                it.copy(isPrinting = true, printedCount = 0, totalCount = printCount)
            }

            printToDevice(printer, urunNumber, printCount)
        }
    }

    /**
     * Cihaza yazdırma işlemi
     */
    private suspend fun printToDevice(device: android.bluetooth.BluetoothDevice, urunNumber: String, printCount: Int) {
        try {
            Log.d(TAG, "${device.name} cihazına yazdırılıyor: $urunNumber x $printCount")

            var successCount = 0

            // AYNI NUMARAYI İSTENEN ADET KADAR YAZDIR
            for (i in 1..printCount) {
                Log.d(TAG, "Yazdırılıyor: $urunNumber (${i}/$printCount)")

                val success = printerService.printUrunEtiketBluetooth(
                    device = device,
                    urunNumber = urunNumber,
                    currentIndex = i,
                    totalCount = printCount
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
                    Log.e(TAG, "Yazdırma hatası: $urunNumber (${i}/$printCount)")
                }
            }

            _printerState.update { it.copy(isPrinting = false) }

            if (successCount == printCount) {
                _events.emit(UrunEtiketEvent.PrintSuccess(successCount))

                // Başarılı yazdırma sonrası formu temizle
                _uiState.update {
                    it.copy(
                        lastGeneratedNumbers = emptyList(),
                        lastPrintNumber = null,
                        printCount = 0
                    )
                }
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