package com.example.stokkontrolveyonetimsistemi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : ComponentActivity() {

    private var onBarcodeScanned: ((String) -> Unit)? = null

    // Kamera barkod okuma launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Barkod okuma iptal edildi", Toast.LENGTH_SHORT).show()
        } else {
            val scannedCode = result.contents
            Toast.makeText(this, "Kamera: $scannedCode", Toast.LENGTH_LONG).show()
            onBarcodeScanned?.invoke(scannedCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StokKontrolVeYonetimSistemiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BarcodeReaderScreen(
                        modifier = Modifier.padding(innerPadding),
                        onCameraScan = { callback ->
                            onBarcodeScanned = callback
                            startCameraScan()
                        }
                    )
                }
            }
        }
    }

    private fun startCameraScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Barkodu kamera ile okutun")
            setCameraId(0) // Arka kamera
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(false)
            setTimeout(30000) // 30 saniye timeout
        }
        barcodeLauncher.launch(options)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeReaderScreen(
    modifier: Modifier = Modifier,
    onCameraScan: ((String) -> Unit) -> Unit = {}
) {
    var barcodeInput by remember { mutableStateOf("") }
    var barcodeList by remember { mutableStateOf(listOf<String>()) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // Kamera callback'ini kaydet
    LaunchedEffect(Unit) {
        onCameraScan { scannedCode ->
            barcodeList = barcodeList + scannedCode
        }
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Başlık
        Text(
            text = "Barkod Okuyucu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Barkod giriş alanı
        OutlinedTextField(
            value = barcodeInput,
            onValueChange = { newValue ->
                // Enter tuşu ile biten girdileri işle
                if (newValue.contains("\n") || newValue.contains("\r")) {
                    val cleanCode = newValue.replace("\n", "").replace("\r", "").trim()
                    if (cleanCode.isNotEmpty()) {
                        // Listeye ekle
                        barcodeList = barcodeList + cleanCode
                        // Input'u temizle
                        barcodeInput = ""
                        Toast.makeText(context, "Fiziksel: $cleanCode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    barcodeInput = newValue
                }
            },
            label = { Text("Barkod") },
            placeholder = { Text("Fiziksel okuyucu ile okutun veya manuel girin...") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Scanner,
                    contentDescription = "Barkod"
                )
            },
            trailingIcon = {
                if (barcodeInput.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            if (barcodeInput.trim().isNotEmpty()) {
                                barcodeList = barcodeList + barcodeInput.trim()
                                Toast.makeText(context, "Manuel: ${barcodeInput.trim()}", Toast.LENGTH_SHORT).show()
                                barcodeInput = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ekle"
                        )
                    }
                }
            },
            singleLine = true
        )

        // Kontrol butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Kamera ile oku butonu
            ElevatedButton(
                onClick = {
                    onCameraScan { scannedCode ->
                        barcodeList = barcodeList + scannedCode
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kamera")
            }

            // Odakla butonu
            OutlinedButton(
                onClick = {
                    focusRequester.requestFocus()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Odakla")
            }
        }

        // İkinci buton satırı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Temizle butonu
            OutlinedButton(
                onClick = {
                    barcodeList = emptyList()
                    barcodeInput = ""
                    Toast.makeText(context, "Tüm veriler temizlendi", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                enabled = barcodeList.isNotEmpty() || barcodeInput.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Temizle")
            }

            // İstatistik gösterici
            OutlinedButton(
                onClick = {
                    val message = if (barcodeList.isEmpty()) {
                        "Henüz barkod taranmadı"
                    } else {
                        "Toplam ${barcodeList.size} barkod tarandı"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("İstatistik")
            }
        }

        // Barkod listesi
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Liste başlığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Taranan Barkodlar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (barcodeList.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${barcodeList.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Liste içeriği
                if (barcodeList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Henüz barkod taranmadı",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kamera veya fiziksel okuyucu kullanın",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(barcodeList.reversed()) { barcode ->
                            BarcodeListItem(
                                barcode = barcode,
                                index = barcodeList.size - barcodeList.reversed().indexOf(barcode),
                                onDelete = {
                                    barcodeList = barcodeList.filter { it != barcode }
                                },
                                onCopy = {
                                    // Kopyalama işlemi için Toast göster
                                    Toast.makeText(context, "Kopyalandı: $barcode", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarcodeListItem(
    barcode: String,
    index: Int,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sıra numarası
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "$index",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Barkod metni
                Text(
                    text = barcode,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Row {
                // Kopyala butonu
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopyala",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Silme butonu
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarcodeReaderScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        BarcodeReaderScreen()
    }
}