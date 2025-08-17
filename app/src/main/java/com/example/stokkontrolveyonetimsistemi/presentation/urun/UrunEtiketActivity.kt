package com.example.stokkontrolveyonetimsistemi.presentation.urun

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.stokkontrolveyonetimsistemi.data.model.product.UrunEtiketEvent
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ÜRÜN Etiket Activity - Bluetooth Printer Destekli
 * Adet girip standart boyutta ÜRÜN etiketi üretme ve otomatik yazdırma
 */
class UrunEtiketActivity : ComponentActivity() {

    companion object {
        private const val TAG = "UrunEtiketActivity"
    }

    // ViewModel injection
    private val viewModel: UrunEtiketViewModel by viewModel()

    // Bluetooth izin launcher'ı - üretim için
    private val bluetoothPermissionForGenerateLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "Bluetooth izinleri verildi - üretim başlatılıyor")
            // İzin verildiyse üretimi başlat
            handleGenerateWithPermission()
        } else {
            Log.e(TAG, "Bluetooth izinleri reddedildi")
            Toast.makeText(this, "Bluetooth izinleri gerekli", Toast.LENGTH_LONG).show()
        }
    }

    // Geçici adet değeri
    private var pendingAdet: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "UrunEtiketActivity created")

        // Event'leri dinle
        observeEvents()

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    val printerState by viewModel.printerState.collectAsState()

                    UrunEtiketScreen(
                        uiState = uiState,
                        printerState = printerState,
                        onAdetChanged = { /* Input handling in screen */ },
                        onGenerateClick = { adet ->
                            // Üret butonuna basıldığında önce Bluetooth izinlerini kontrol et
                            pendingAdet = adet
                            if (checkBluetoothPermissions()) {
                                viewModel.generateUrunEtiketleri(adet)
                            } else {
                                requestBluetoothPermissions()
                            }
                        },
                        onBackClick = {
                            onBackPressedDispatcher.onBackPressed()
                        },
                        onClearError = {
                            viewModel.clearError()  // ✅ ViewModel'deki clearError fonksiyonu çağrılıyor
                        }
                    )
                }
            }
        }
    }

    /**
     * Event'leri dinle - TÜM EVENT'LER HANDLE EDİLDİ
     */
    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is UrunEtiketEvent.GenerationSuccess -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "${event.count} adet ÜRÜN numarası üretildi (${event.number})",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Üretim başarılı: ${event.count} adet, Numara: ${event.number}")
                    }

                    is UrunEtiketEvent.PrintSuccess -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "${event.count} adet etiket yazdırıldı",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Yazdırma başarılı: ${event.count} adet")
                    }

                    is UrunEtiketEvent.Error -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "Hata: ${event.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, "Üretim hatası: ${event.message}")
                    }

                    is UrunEtiketEvent.PrintError -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "Yazdırma hatası: ${event.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, "Yazdırma hatası: ${event.message}")
                    }

                    UrunEtiketEvent.PrinterConnected -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "Printer bağlandı",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Printer bağlantısı başarılı")
                    }

                    // ✅ YENİ EKLENEN EVENT'LER
                    UrunEtiketEvent.PrinterDisconnected -> {
                        Toast.makeText(
                            this@UrunEtiketActivity,
                            "Printer bağlantısı kesildi",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Printer bağlantısı kesildi")
                    }

                    is UrunEtiketEvent.PrintProgress -> {
                        // Progress güncellemesi - Toast göstermiyoruz, sadece log
                        Log.d(TAG, "Yazdırma ilerleme: ${event.current}/${event.total}")
                        // UI'da zaten progress bar gösteriliyor
                    }
                }
            }
        }
    }

    /**
     * Bluetooth izinlerini kontrol et
     */
    private fun checkBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Bluetooth izinlerini iste (üretim için)
     */
    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        Log.d(TAG, "Bluetooth izinleri isteniyor (üretim için)")
        bluetoothPermissionForGenerateLauncher.launch(permissions)
    }

    /**
     * İzin verildikten sonra üretimi başlat
     */
    private fun handleGenerateWithPermission() {
        if (pendingAdet > 0) {
            viewModel.generateUrunEtiketleri(pendingAdet)
            pendingAdet = 0 // Reset
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "UrunEtiketActivity destroyed")
    }
}