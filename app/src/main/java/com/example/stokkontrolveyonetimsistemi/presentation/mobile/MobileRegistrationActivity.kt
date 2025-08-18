package com.example.stokkontrolveyonetimsistemi.presentation.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stokkontrolveyonetimsistemi.presentation.location.LocationActivity
import com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens.*
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Mobile Registration Activity
 * Ürün ekleme workflow'u için ana activity
 * Compose Navigation kullanır
 */
class MobileRegistrationActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MobileRegistrationActivity"
    }

    // ViewModel
    private val viewModel: MobileRegistrationViewModel by viewModel()

    // Barkod okuyucu callback'leri
    private var onRafBarcodeScanned: ((String) -> Unit)? = null
    private var onUrunBarcodeScanned: ((String) -> Unit)? = null

    // RAF Barkod okuyucu launcher
    private val rafBarcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedCode = result.contents
            Log.d(TAG, "RAF Barkodu: $scannedCode")
            onRafBarcodeScanned?.invoke(scannedCode)
        } else {
        }
    }

    // Ürün Barkod okuyucu launcher
    private val urunBarcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedCode = result.contents
            Log.d(TAG, "Ürün Barkodu: $scannedCode")
            onUrunBarcodeScanned?.invoke(scannedCode)
        } else {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MobileRegistrationActivity started")

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MobileRegistrationNavHost()
                }
            }
        }
    }

    /**
     * Navigation Host
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MobileRegistrationNavHost() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                // TopBar sadece bazı ekranlarda gösterilsin
                if (currentRoute != MobileNavigation.LocationCheck.route) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentRoute) {
                                    MobileNavigation.RafScan.route -> "RAF Numarası"
                                    MobileNavigation.UrunBarcode.route -> "Ürün Barkodu"
                                    MobileNavigation.UrunDetail.route -> "Ürün Detayları"
                                    MobileNavigation.PhotoCapture.route -> "Fotoğraf Çekimi"
                                    MobileNavigation.ReviewSubmit.route -> "Özet ve Kayıt"
                                    else -> "Ürün Ekleme"
                                }
                            )
                        },
                        navigationIcon = {
                            if (currentRoute != MobileNavigation.LocationCheck.route) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Geri"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = MobileNavigation.LocationCheck.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // 1. Lokasyon Kontrolü
                composable(MobileNavigation.LocationCheck.route) {
                    LocationCheckScreen(
                        viewModel = viewModel,
                        onNavigateToRafScan = {
                            navController.navigate(MobileNavigation.RafScan.route)
                        },
                        onOpenLocationActivity = {
                            openLocationActivity()
                        }
                    )
                }

                // 2. RAF Barkod Okuma
                composable(MobileNavigation.RafScan.route) {
                    RafScanScreen(
                        viewModel = viewModel,
                        onScanBarcode = { callback ->
                            onRafBarcodeScanned = callback
                            startRafBarcodeScanner()
                        },
                        onNavigateToUrunBarcode = {
                            navController.navigate(MobileNavigation.UrunBarcode.route)
                        }
                    )
                }

                // 3. Ürün Barkod Okuma
                composable(MobileNavigation.UrunBarcode.route) {
                    UrunBarcodeScreen(
                        viewModel = viewModel,
                        onScanBarcode = { callback ->
                            onUrunBarcodeScanned = callback
                            startUrunBarcodeScanner()
                        },
                        onNavigateToUrunDetail = {
                            navController.navigate(MobileNavigation.UrunDetail.route)
                        }
                    )
                }

                // 4. Ürün Detayları
                composable(MobileNavigation.UrunDetail.route) {
                    UrunDetailScreen(
                        viewModel = viewModel,
                        onNavigateToPhotoCapture = {
                            navController.navigate(MobileNavigation.PhotoCapture.route)
                        }
                    )
                }

                // 5. Fotoğraf Çekimi
                composable(MobileNavigation.PhotoCapture.route) {
                    PhotoCaptureScreen(
                        viewModel = viewModel,
                        onNavigateToReview = {
                            navController.navigate(MobileNavigation.ReviewSubmit.route)
                        }
                    )
                }

                // 6. Özet ve Kayıt
                composable(MobileNavigation.ReviewSubmit.route) {
                    ReviewSubmitScreen(
                        viewModel = viewModel,
                        onNavigateToStep = { route ->
                            navController.navigate(route) {
                                popUpTo(MobileNavigation.LocationCheck.route)
                            }
                        },
                        onRegistrationComplete = {
                            // Başarılı kayıt sonrası başa dön
                            navController.navigate(MobileNavigation.LocationCheck.route) {
                                popUpTo(MobileNavigation.LocationCheck.route) {
                                    inclusive = true
                                }
                            }
                            viewModel.resetWorkflow()
                        }
                    )
                }
            }
        }
    }

    /**
     * RAF barkod okuyucuyu başlat
     */
    private fun startRafBarcodeScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("RAF barkodunu okutun")
            setCameraId(0) // Arka kamera
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(false)
            setTimeout(30000) // 30 saniye timeout
        }
        rafBarcodeLauncher.launch(options)
    }

    /**
     * Ürün barkod okuyucuyu başlat
     */
    private fun startUrunBarcodeScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Ürün barkodunu okutun")
            setCameraId(0) // Arka kamera
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(false)
            setTimeout(30000) // 30 saniye timeout
        }
        urunBarcodeLauncher.launch(options)
    }

    /**
     * LocationActivity'yi aç
     */
    private fun openLocationActivity() {
        try {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening LocationActivity", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // LocationActivity'den dönüşte session'ı yenile
        viewModel.checkUserSession()
    }
}

/**
 * Navigation routes
 */
object MobileNavigation {
    object LocationCheck {
        const val route = "location_check"
    }

    object RafScan {
        const val route = "raf_scan"
    }

    object UrunBarcode {
        const val route = "urun_barcode"
    }

    object UrunDetail {
        const val route = "urun_detail"
    }

    object PhotoCapture {
        const val route = "photo_capture"
    }

    object ReviewSubmit {
        const val route = "review_submit"
    }
}