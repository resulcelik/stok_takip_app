package com.example.stokkontrolveyonetimsistemi.presentation.rafetiket

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stokkontrolveyonetimsistemi.data.model.product.PrinterState
import com.example.stokkontrolveyonetimsistemi.data.model.shelf.RafEtiketResult
import com.example.stokkontrolveyonetimsistemi.data.model.shelf.RafEtiketUiState  // ✅ EKLENEN IMPORT
import com.example.stokkontrolveyonetimsistemi.data.repository.RafEtiketRepository
import com.example.stokkontrolveyonetimsistemi.printer.TscAlpha3RPrinterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * RAF Etiket ViewModel - Bluetooth Printer Desteği
 * Terminal'den Bluetooth ile TSC Alpha-3R'a yazdırma
 */
class RafEtiketViewModel(
    private val repository: RafEtiketRepository,
    private val printerService: TscAlpha3RPrinterService
) : ViewModel() {

    companion object {
        private const val TAG = "RafEtiketViewModel"
    }

    private val _uiState = MutableStateFlow(RafEtiketUiState())
    val uiState: StateFlow<RafEtiketUiState> = _uiState.asStateFlow()

    /**
     * Adet değişikliği
     */
    fun onAdetChanged(newAdet: String) {
        // Sadece sayı kabul et
        val filtered = newAdet.filter { it.isDigit() }

        // Maksimum 4 karakter (1000'e kadar)
        val limited = if (filtered.length > 4) filtered.take(4) else filtered

        _uiState.update { it.copy(adet = limited, errorMessage = null) }
    }

    /**
     * RAF etiketi üret ve otomatik yazdır
     */
    fun generateRafEtiketleri() {
        val adetInt = _uiState.value.adet.toIntOrNull() ?: 0

        // Validasyon
        if (adetInt !in 1..1000) {
            _uiState.update {
                it.copy(errorMessage = "Lütfen 1-1000 arası bir sayı girin")
            }
            return
        }

        viewModelScope.launch {
            repository.generateRafEtiketleri(adetInt).collect { result ->
                when (result) {
                    is RafEtiketResult.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true, errorMessage = null)
                        }
                    }

                    is RafEtiketResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                rafNumbers = result.data,
                                totalCount = result.data.size,
                                errorMessage = null
                            )
                        }
                        Log.d(TAG, "${result.data.size} adet RAF etiketi üretildi")

                        // ✅ OTOMATİK YAZDIR - Üretim başarılı olduktan sonra
                        printWithBluetooth()
                    }

                    is RafEtiketResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                                rafNumbers = emptyList()
                            )
                        }
                        Log.e(TAG, "Hata: ${result.message}")
                    }
                }
            }
        }
    }

    /**
     * Bluetooth ile etiketleri yazdır
     */
    fun printWithBluetooth() {
        val rafNumbers = _uiState.value.rafNumbers

        if (rafNumbers.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Yazdırılacak etiket yok")
            }
            return
        }

        // Bluetooth kontrolü
        if (!printerService.isBluetoothAvailable()) {
            _uiState.update {
                it.copy(errorMessage = "Bluetooth kapalı veya kullanılamıyor")
            }
            return
        }

        if (!printerService.hasBluetoothPermissions()) {
            _uiState.update {
                it.copy(errorMessage = "Bluetooth izinleri gerekli")
            }
            return
        }

        viewModelScope.launch {
            // TSC veya SPP printer bul
            val printer = printerService.findPairedTscPrinter()

            if (printer == null) {
                // Hiç cihaz yoksa hata
                val pairedDevices = printerService.getPairedDevices()
                if (pairedDevices.isEmpty()) {
                    _uiState.update {
                        it.copy(errorMessage = "Eşleştirilmiş Bluetooth cihaz yok. Ayarlardan printer'ı eşleştirin.")
                    }
                } else {
                    // Cihaz var ama printer değil - ilkini kullan
                    Log.w(TAG, "Printer bulunamadı, ${pairedDevices.size} cihaz var")
                    _uiState.update {
                        it.copy(errorMessage = "TSC printer bulunamadı. BT-SPP cihazı kullanılacak.")
                    }
                    // İlk cihazı kullanmayı dene
                    pairedDevices.firstOrNull()?.let { device ->
                        _uiState.update {
                            it.copy(isPrinting = true, printedCount = 0, errorMessage = null)
                        }
                        printToDevice(device, rafNumbers)
                    }
                }
                return@launch
            }

            // Printer bulundu - yazdırmaya başla
            Log.d(TAG, "Printer bulundu: ${printer.name} - yazdırma başlıyor")
            _uiState.update {
                it.copy(isPrinting = true, printedCount = 0, errorMessage = null)
            }

            printToDevice(printer, rafNumbers)
        }
    }

    /**
     * Seçili cihaza yazdır
     */
    fun printToSelectedDevice(device: BluetoothDevice) {
        val rafNumbers = _uiState.value.rafNumbers

        if (rafNumbers.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Yazdırılacak etiket yok")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isPrinting = true, printedCount = 0, errorMessage = null)
            }

            printToDevice(device, rafNumbers)
        }
    }

    /**
     * Cihaza yazdırma işlemi
     */
    private suspend fun printToDevice(device: BluetoothDevice, rafNumbers: List<String>) {
        try {
            Log.d(TAG, "${device.name} cihazına yazdırılıyor: ${rafNumbers.size} etiket")

            // Her etiket için yazdır
            rafNumbers.forEachIndexed { index, rafNumber ->
                val success = printerService.printRafEtiketBluetooth(
                    device = device,
                    rafNumber = rafNumber,
                    currentIndex = index + 1,
                    totalCount = rafNumbers.size
                )

                if (success) {
                    _uiState.update {
                        it.copy(printedCount = index + 1)
                    }
                } else {
                    throw Exception("Yazdırma hatası: $rafNumber")
                }
            }

            // Başarılı - formu sıfırla
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    rafNumbers = emptyList(),
                    adet = "",
                    printedCount = 0,
                    totalCount = 0
                )
            }

            Log.d(TAG, "Tüm etiketler yazdırıldı")

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isPrinting = false,
                    errorMessage = "Yazdırma hatası: ${e.message}"
                )
            }
            Log.e(TAG, "Yazdırma hatası", e)
        }
    }

    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Formu sıfırla
     */
    fun resetForm() {
        _uiState.value = RafEtiketUiState()
    }



}