package com.example.stokkontrolveyonetimsistemi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : ComponentActivity() {
    private var barcodeBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StockManagementMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Fiziksel barkod sensörü için KeyEvent kontrolü
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            // Yaygın barkod sensörü tuş kodları
            KeyEvent.KEYCODE_F1,
            KeyEvent.KEYCODE_F2,
            KeyEvent.KEYCODE_F3,
            KeyEvent.KEYCODE_CTRL_LEFT,
            KeyEvent.KEYCODE_CTRL_RIGHT -> {
                Toast.makeText(this, "Barkod sensörü tetiklendi!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Bazı cihazlarda barkod verisi karakter karakter gelir
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // Enter tuşu geldiğinde barkod tamamlanmış demektir
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            val scannedCode = barcodeBuffer.toString()
            if (scannedCode.isNotEmpty()) {
                Toast.makeText(this, "Sensör taraması: $scannedCode", Toast.LENGTH_LONG).show()
                barcodeBuffer.clear()
                return true
            }
        } else {
            val char = event?.unicodeChar?.toChar()
            if (char != null && (char.isLetterOrDigit() || char.isWhitespace() || char in "!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~")) {
                // Alfanumerik karakterleri buffer'a ekle
                barcodeBuffer.append(char)
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun StockManagementMainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf("") }
    var scannedCodes by remember { mutableStateOf(listOf<String>()) }

    // Kamera izni launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
        }
    }

    // Barkod scanner launcher
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Tarama iptal edildi", Toast.LENGTH_SHORT).show()
        } else {
            scannedCode = result.contents
            scannedCodes = scannedCodes + result.contents
            Toast.makeText(context, "Tarandı: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }

    // Barkod tarama fonksiyonu
    fun startBarcodeScanning() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                // İzin var, taramayı başlat
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                    setPrompt("Barkod veya QR kodu tarayın")
                    setCameraId(0)
                    setBeepEnabled(true)
                    setBarcodeImageEnabled(true)
                    setOrientationLocked(false)
                }
                barcodeLauncher.launch(options)
            }
            else -> {
                // İzin iste
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "Stok Kontrol ve Yönetim Sistemi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Ana Menü Butonları
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                MenuButton(
                    text = "Fiziksel Sensör Testi",
                    icon = Icons.Default.Scanner,
                    onClick = {
                        Toast.makeText(context, "Cihazın fiziksel barkod sensörünü kullanın", Toast.LENGTH_LONG).show()
                    }
                )
            }

            item {
                MenuButton(
                    text = "Barkod/QR Kod Tara",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = { startBarcodeScanning() }
                )
            }

            item {
                MenuButton(
                    text = "Stok Listesi",
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = {
                        Toast.makeText(context, "Stok Listesi - Geliştiriliyor", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                MenuButton(
                    text = "Yeni Ürün Ekle",
                    icon = Icons.Default.Add,
                    onClick = {
                        Toast.makeText(context, "Yeni Ürün Ekle - Geliştiriliyor", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                MenuButton(
                    text = "Raporlar",
                    icon = Icons.Default.Assessment,
                    onClick = {
                        Toast.makeText(context, "Raporlar - Geliştiriliyor", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                MenuButton(
                    text = "Ayarlar",
                    icon = Icons.Default.Settings,
                    onClick = {
                        Toast.makeText(context, "Ayarlar - Geliştiriliyor", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Son taranmış kod gösterimi
        if (scannedCode.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Son Taranan Kod:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = scannedCode,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Taranan kodlar listesi
        if (scannedCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Taranan Kodlar (${scannedCodes.size}):",
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(scannedCodes.size) { index ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "${index + 1}. ${scannedCodes[index]}",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StockManagementMainScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        StockManagementMainScreen()
    }
}