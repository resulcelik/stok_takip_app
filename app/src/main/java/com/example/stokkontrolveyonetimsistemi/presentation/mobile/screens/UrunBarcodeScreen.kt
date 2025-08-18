package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel

// Ürün Numarası Gösterimi (Barkod okutulduğunda)@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrunBarcodeScreen(
    viewModel: MobileRegistrationViewModel,
    onScanBarcode: (callback: (String) -> Unit) -> Unit,
    onNavigateToUrunDetail: () -> Unit
) {
    // State'leri observe et
    val rafSeriNo by viewModel.rafSeriNo.collectAsState()
    val urunBilgileri by viewModel.urunBilgileri.collectAsState()
    val isUrunBarcodeScanned by viewModel.isUrunBarcodeScanned.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    val context = LocalContext.current

    // Text field için state
    var barcodeInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Success message handling
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            if (message.contains("Ürün barkodu okundu")) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }

    // **YENİ: isUrunBarcodeScanned state değişimini takip et**
    LaunchedEffect(isUrunBarcodeScanned) {
        Log.d("UrunBarcodeScreen", "isUrunBarcodeScanned changed: $isUrunBarcodeScanned")
    }

    // Otomatik odaklan
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // RAF Bilgisi (Üstte göster)
        if (rafSeriNo.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RAF: $rafSeriNo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Başlık
        Text(
            text = "📦 ÜRÜN BARKODU OKUTUN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Açıklama
        Text(
            text = "Fiziksel okuyucu ile ürün barkodunu okutun",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // **DÜZELTİLDİ: Barkod Input Field**
        OutlinedTextField(
            value = barcodeInput,
            onValueChange = { newValue ->
                // Enter tuşu ile biten girdileri işle
                if (newValue.contains("\n") || newValue.contains("\r")) {
                    val cleanCode = newValue.replace("\n", "").replace("\r", "").trim()
                    if (cleanCode.isNotEmpty()) {
                        if (isValidUrunFormat(cleanCode)) {
                            // **ÖNEMLİ: Ürün barkodunu process et ve input'u temizle**
                            viewModel.processUrunBarcode(cleanCode)
                            barcodeInput = "" // Input'u temizle

                            // **YENİ: Barcode process edildiğinde log ekle**
                            Log.d("UrunBarcodeScreen", "Barcode processed: $cleanCode")
                        } else {
                            Toast.makeText(context, "Geçersiz ürün formatı! U ile başlamalı ve 12 karakter olmalı.", Toast.LENGTH_LONG).show()
                            barcodeInput = ""
                        }
                    }
                } else {
                    barcodeInput = newValue
                }
            },
            label = { Text("Ürün Barkodu (U00000000XXX)") },
            placeholder = { Text("Fiziksel okuyucu ile okutun...") },
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
            singleLine = true,
            enabled = !isLoading,

        )

        Spacer(modifier = Modifier.height(16.dp))

        // **YENİ: Manuel Test Butonu (Geliştirme için)**
        if (!isUrunBarcodeScanned && urunBilgileri.tasnifNo.isEmpty()) {
            OutlinedButton(
                onClick = {
                    // Test için manuel barcode ekleme
                    viewModel.processUrunBarcode(barcodeInput)
                    Toast.makeText(context, "Test barkodu eklendi: $barcodeInput", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                border = BorderStroke(1.dp, Color.Green)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEST: Manuel Barcode Ekle", color = Color.Green)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Odakla Butonu
        OutlinedButton(
            onClick = {
                focusRequester.requestFocus()
                barcodeInput = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Odağı Yenile")
        }

        // **DÜZELTİLDİ: Ürün Numarası Gösterimi - Koşul basitleştirildi**
        if (urunBilgileri.tasnifNo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Color(0xFF00897B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrunBarcodeScanned) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isUrunBarcodeScanned) Color(0xFF00897B) else Color.Green,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isUrunBarcodeScanned) "Ürün Numarası Okundu" else "Ürün Numarası (Doğrulanmadı)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUrunBarcodeScanned) Color(0xFF00897B) else Color.Green
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Okunan Ürün Numarası:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ürün Numarası
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = urunBilgileri.tasnifNo,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Yeniden Okut Butonu
            OutlinedButton(
                onClick = {
                    // Ürün barkodunu temizle ve odaklan
                    viewModel.clearUrunBarcode()
                    barcodeInput = ""
                    focusRequester.requestFocus()
                    Toast.makeText(context, "Yeni ürün okutabilirsiniz", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                border = BorderStroke(1.dp, Color(0xFF00897B))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF00897B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "YENİDEN OKUT",
                    color = Color(0xFF00897B)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // **GELİŞTİRİLDİ: Debug bilgisi göster - Daha kompakt**
        if (urunBilgileri.tasnifNo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("DEBUG:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("tasnifNo: '${urunBilgileri.tasnifNo}'", fontSize = 10.sp)
                    Text("isUrunBarcodeScanned: $isUrunBarcodeScanned", fontSize = 10.sp)
                    Text("Button enabled: ${urunBilgileri.tasnifNo.isNotEmpty() && isUrunBarcodeScanned && !isLoading}", fontSize = 10.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Devam Et Butonu (Orijinal kodunuzdaki gibi, sadece koşul düzeltildi)
        Button(
            onClick = {
                // Navigation state'i güncelle ve devam et
                viewModel.processUrunBarcode(barcodeInput)
                viewModel.navigateToNextStep()
                onNavigateToUrunDetail()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            //enabled = urunBilgileri.tasnifNo.isNotEmpty() && isUrunBarcodeScanned && !isLoading, // Orijinal mantığınız
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Gray
            )
        ) {
            Text(
                text = "DEVAM ET",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        // **YENİ: Force Enable Butonu (Debug için)**
        if (urunBilgileri.tasnifNo.isNotEmpty() && !isUrunBarcodeScanned) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    // Force olarak isUrunBarcodeScanned'i true yap
                    Toast.makeText(context, "Force enabling - Bu sadece debug için!", Toast.LENGTH_SHORT).show()

                    // Eğer ViewModel'de public bir setter varsa kullan, yoksa alternatif çözüm
                    // viewModel.forceEnableUrunBarcode() // Bu metodu ViewModel'e eklemelisiniz

                    viewModel.navigateToNextStep()
                    onNavigateToUrunDetail()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔧 DEBUG: FORCE ENABLE & CONTINUE",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Error handling
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Hata") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Tamam")
                }
            }
        )
    }
}

/**
 * Ürün format kontrolü
 */
private fun isValidUrunFormat(urunNo: String): Boolean {
    return urunNo.startsWith("U") && urunNo.length == 12
}