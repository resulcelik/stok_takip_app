package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel

/**
 * Ürün Detail Screen
 * Ürün detay bilgileri giriş ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrunDetailScreen(
    viewModel: MobileRegistrationViewModel,
    onNavigateToPhotoCapture: () -> Unit
) {
    // State'leri observe et
    val rafSeriNo by viewModel.rafSeriNo.collectAsState()
    val urunBilgileri by viewModel.urunBilgileri.collectAsState()
    val stokBirimleri by viewModel.stokBirimleri.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    // Form state'leri - Başlangıç değerleri ViewModel'den al
    var aciklama by remember { mutableStateOf(urunBilgileri.aciklama) }
    var selectedStokBirimi by remember { mutableStateOf<Long?>(
        if (urunBilgileri.stokBirimiId > 0) urunBilgileri.stokBirimiId else null
    ) }
    var selectedStokBirimi2 by remember { mutableStateOf<Long?>(urunBilgileri.stokBirimi2Id) }
    var en by remember { mutableStateOf(urunBilgileri.en?.toString() ?: "") }
    var boy by remember { mutableStateOf(urunBilgileri.boy?.toString() ?: "") }
    var yukseklik by remember { mutableStateOf(urunBilgileri.yukseklik?.toString() ?: "") }

    // Dropdown state'leri
    var expandedStokBirimi by remember { mutableStateOf(false) }
    var expandedStokBirimi2 by remember { mutableStateOf(false) }

    // Form validation
    var aciklamaError by remember { mutableStateOf<String?>(null) }
    var stokBirimiError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // RAF ve Ürün Bilgisi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RAF Bilgisi
            if (rafSeriNo.isNotEmpty()) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RAF: ${rafSeriNo.takeLast(6)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Ürün Numarası
            if (urunBilgileri.tasnifNo.isNotEmpty()) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F2F1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ürün: ${urunBilgileri.tasnifNo.takeLast(6)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Başlık
        Text(
            text = "📝 ÜRÜN DETAYLARI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Ürün Numarası Kartı (Değiştirilemez)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "📦 Ürün Numarası",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = urunBilgileri.tasnifNo.ifEmpty { "-" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "✓ Barkoddan okundu",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form Alanları

        // Açıklama
        OutlinedTextField(
            value = aciklama,
            onValueChange = {
                aciklama = it
                aciklamaError = when {
                    it.trim().isEmpty() -> "Açıklama zorunludur"
                    it.trim().length < 3 -> "En az 3 karakter olmalıdır"
                    else -> null
                }
            },
            label = { Text("Açıklama *") },
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = null)
            },
            isError = aciklamaError != null,
            supportingText = {
                aciklamaError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stok Birimi Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedStokBirimi,
            onExpandedChange = { expandedStokBirimi = it }
        ) {
            OutlinedTextField(
                value = stokBirimleri.find { it.id == selectedStokBirimi }?.stokBirimiAdi ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Stok Birimi *") },
                leadingIcon = {
                    Icon(Icons.Default.Inventory2, contentDescription = null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStokBirimi)
                },
                isError = stokBirimiError != null,
                supportingText = {
                    stokBirimiError?.let { Text(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedStokBirimi,
                onDismissRequest = { expandedStokBirimi = false }
            ) {
                stokBirimleri.forEach { birim ->
                    DropdownMenuItem(
                        text = { Text(birim.stokBirimiAdi) },
                        onClick = {
                            selectedStokBirimi = birim.id
                            stokBirimiError = null
                            expandedStokBirimi = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stok Birimi 2 (Opsiyonel)
        ExposedDropdownMenuBox(
            expanded = expandedStokBirimi2,
            onExpandedChange = { expandedStokBirimi2 = it }
        ) {
            OutlinedTextField(
                value = stokBirimleri.find { it.id == selectedStokBirimi2 }?.stokBirimiAdi ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Stok Birimi 2 (Opsiyonel)") },
                leadingIcon = {
                    Icon(Icons.Default.Inventory, contentDescription = null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStokBirimi2)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedStokBirimi2,
                onDismissRequest = { expandedStokBirimi2 = false }
            ) {
                // İlk item "Seçiniz"
                DropdownMenuItem(
                    text = { Text("Seçiniz") },
                    onClick = {
                        selectedStokBirimi2 = null
                        expandedStokBirimi2 = false
                    }
                )

                stokBirimleri.forEach { birim ->
                    DropdownMenuItem(
                        text = { Text(birim.stokBirimiAdi) },
                        onClick = {
                            selectedStokBirimi2 = birim.id
                            expandedStokBirimi2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boyutlar Başlık
        Text(
            text = "📏 Boyutlar (Opsiyonel)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Boyut Alanları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // En
            OutlinedTextField(
                value = en,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        en = newValue
                    }
                },
                label = { Text("En (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            // Boy
            OutlinedTextField(
                value = boy,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        boy = newValue
                    }
                },
                label = { Text("Boy (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            // Yükseklik
            OutlinedTextField(
                value = yukseklik,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        yukseklik = newValue
                    }
                },
                label = { Text("Yükseklik (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Devam Et Butonu
        Button(
            onClick = {
                // Validation
                var isValid = true

                if (aciklama.trim().isEmpty()) {
                    aciklamaError = "Açıklama zorunludur"
                    isValid = false
                } else if (aciklama.trim().length < 3) {
                    aciklamaError = "En az 3 karakter olmalıdır"
                    isValid = false
                }

                if (selectedStokBirimi == null || selectedStokBirimi == 0L) {
                    stokBirimiError = "Stok birimi seçiniz"
                    isValid = false
                }

                if (isValid) {
                    // ViewModel'e kaydet
                    viewModel.updateUrunBilgileri(
                        aciklama = aciklama.trim(),
                        stokBirimiId = selectedStokBirimi ?: 0L,
                        stokBirimi2Id = selectedStokBirimi2,
                        en = en.toDoubleOrNull(),
                        boy = boy.toDoubleOrNull(),
                        yukseklik = yukseklik.toDoubleOrNull()
                    )

                    // Navigation state'i güncelle ve devam et
                    viewModel.navigateToNextStep()
                    onNavigateToPhotoCapture()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
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