package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel

/**
 * RAF Scan Screen
 * Fiziksel barkod okuyucu ile RAF numarası okuma
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RafScanScreen(
    viewModel: MobileRegistrationViewModel,
    onScanBarcode: (callback: (String) -> Unit) -> Unit,
    onNavigateToUrunBarcode: () -> Unit
) {
    // State'leri observe et
    val rafSeriNo by viewModel.rafSeriNo.collectAsState()
    val isRafSaved by viewModel.isRafSaved.collectAsState()
    val rafData by viewModel.rafData.observeAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    val context = LocalContext.current

    // Text field için state
    var barcodeInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Success mesajını dinle
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            if (message.contains("RAF başarıyla") || message.contains("kaydedildi")) {
                viewModel.clearSuccess()
            }
        }
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
        // Başlık
        Text(
            text = "📋 RAF NUMARASI OKUTUN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Açıklama
        Text(
            text = "Fiziksel okuyucu ile RAF barkodunu okutun",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Barkod Input Field
        OutlinedTextField(
            value = barcodeInput,
            onValueChange = { newValue ->
                // Enter tuşu ile biten girdileri işle
                if (newValue.contains("\n") || newValue.contains("\r")) {
                    val cleanCode = newValue.replace("\n", "").replace("\r", "").trim()
                    if (cleanCode.isNotEmpty() && !isLoading) {
                        if (isValidRafFormat(cleanCode)) {
                            // RAF numarasını process et ve otomatik kaydet
                            viewModel.processRafBarcode(cleanCode)
                            barcodeInput = "" // Input'u temizle

                            // RAF'ı otomatik olarak backend'e kaydet
                            viewModel.saveRafToBackend()
                        } else {
                            barcodeInput = ""
                        }
                    }
                } else {
                    barcodeInput = newValue
                }
            },
            label = { Text("RAF Numarası (R00000000XXX)") },
            placeholder = { Text("Barkod okuyucu ile okutun...") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Manuel RAF Girişi için buton
        OutlinedButton(
            onClick = {
                // Manuel olarak RAF numarası gir
                if (barcodeInput.isNotEmpty() && isValidRafFormat(barcodeInput)) {
                    viewModel.processRafBarcode(barcodeInput)
                    viewModel.saveRafToBackend()
                    barcodeInput = ""
                } else {
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && barcodeInput.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("RAF'I MANUEL KAYDET")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Okunmuş RAF göster
        if (rafSeriNo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRafSaved) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isRafSaved) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isRafSaved) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isRafSaved) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRafSaved) "RAF Kaydedildi:" else "Okunan RAF:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isRafSaved) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = rafSeriNo,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RAF Kaydedilmemişse Kaydet Butonu Göster
            if (!isRafSaved && !isLoading) {
                Button(
                    onClick = {
                        // RAF'ı backend'e kaydet
                        viewModel.saveRafToBackend()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RAF'I KAYDET", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Yeniden Okut Butonu
            OutlinedButton(
                onClick = {
                    // RAF numarasını temizle ve odaklan
                    viewModel.clearRafData()
                    barcodeInput = ""
                    focusRequester.requestFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "YENİDEN OKUT")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sonraki Adıma Geç Butonu - Sadece RAF kaydedildiyse göster
        if (isRafSaved && rafSeriNo.isNotEmpty()) {
            Button(
                onClick = {
                    // Sonraki adıma geçmeden önce navigation metodunu çağır
                    viewModel.navigateToNextStep()
                    onNavigateToUrunBarcode()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "ÜRÜN OKUTMAYA GEÇ",
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
        }

        // Loading durumu
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "RAF kaydediliyor...",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Error handling
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hata")
                }
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearError()
                        focusRequester.requestFocus()
                    }
                ) {
                    Text("Tamam")
                }
            }
        )
    }
}

/**
 * RAF format kontrolü
 */
private fun isValidRafFormat(rafNo: String): Boolean {
    return rafNo.startsWith("R") && rafNo.length == 12
}