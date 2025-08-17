package com.example.stokkontrolveyonetimsistemi.presentation.rafetiket

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
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * RAF Etiket Activity - Bluetooth Printer Desteği ile
 * Sadece adet girip standart boyutta RAF etiketi üretme
 */
class RafEtiketActivity : ComponentActivity() {

    companion object {
        private const val TAG = "RafEtiketActivity"
    }

    // ViewModel injection
    private val viewModel: RafEtiketViewModel by viewModel()

    // Bluetooth izin launcher'ı - generateRafEtiketleri için
    private val bluetoothPermissionForGenerateLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "Bluetooth izinleri verildi - üretim başlatılıyor")
            // İzin verildiyse üretimi başlat
            viewModel.generateRafEtiketleri()
        } else {
            Log.e(TAG, "Bluetooth izinleri reddedildi")
            Toast.makeText(this, "Bluetooth izinleri gerekli", Toast.LENGTH_LONG).show()
        }
    }

    // Bluetooth izin launcher'ı - manuel yazdırma için
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "Bluetooth izinleri verildi")
            Toast.makeText(this, "Bluetooth izinleri verildi", Toast.LENGTH_SHORT).show()
            // İzin verildiyse yazdırmayı başlat
            viewModel.printWithBluetooth()
        } else {
            Log.e(TAG, "Bluetooth izinleri reddedildi")
            Toast.makeText(this, "Bluetooth izinleri gerekli", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "RafEtiketActivity created")

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    RafEtiketScreen(
                        uiState = uiState,
                        onAdetChanged = viewModel::onAdetChanged,
                        onGenerateClick = {
                            // Üret butonuna basıldığında önce Bluetooth izinlerini kontrol et
                            if (checkBluetoothPermissions()) {
                                viewModel.generateRafEtiketleri()
                            } else {
                                requestBluetoothPermissions()
                            }
                        },
                        onPrintClick = {
                            // Manuel yazdırma (opsiyonel)
                            checkBluetoothPermissionsAndPrint()
                        },
                        onBackClick = { onBackPressed() },
                        onClearError = viewModel::clearError
                    )
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
     * Bluetooth izinlerini kontrol et ve yazdır (manuel)
     */
    private fun checkBluetoothPermissionsAndPrint() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 ve üstü
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            // Android 11 ve altı
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        // İzinleri kontrol et
        val hasAllPermissions = permissions.all { permission ->
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (hasAllPermissions) {
            // İzinler var, yazdırmayı başlat
            viewModel.printWithBluetooth()
        } else {
            // İzin iste
            Log.d(TAG, "Bluetooth izinleri isteniyor")
            bluetoothPermissionLauncher.launch(permissions)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG, "Back pressed - returning to dashboard")
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RafEtiketActivity destroyed")
    }
}