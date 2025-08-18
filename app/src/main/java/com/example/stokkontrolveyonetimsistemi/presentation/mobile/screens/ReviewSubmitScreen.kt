package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stokkontrolveyonetimsistemi.presentation.mobile.MobileNavigation
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.UploadStatus

/**
 * Review Submit Screen
 * Özet görüntüleme ve kayıt tamamlama ekranı
 */
@Composable
fun ReviewSubmitScreen(
    viewModel: MobileRegistrationViewModel,
    onNavigateToStep: (String) -> Unit,
    onRegistrationComplete: () -> Unit
) {
    // State'leri observe et - ViewModel'deki güncel değişkenler
    val rafSeriNo by viewModel.rafSeriNo.collectAsState()
    val urunBilgileri by viewModel.urunBilgileri.collectAsState()
    val uploadedPhotos by viewModel.uploadedPhotos.collectAsState()
    val stokBirimleri by viewModel.stokBirimleri.observeAsState(emptyList())
    val createdUrunSeriNo by viewModel.createdUrunSeriNo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()

    // Dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Registration başarılı olduğunda
    LaunchedEffect(createdUrunSeriNo) {
        createdUrunSeriNo?.let {
            isSuccess = true
        }
    }

    if (!isSuccess) {
        // Normal özet ekranı
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Başlık
            Text(
                text = "📋 KAYIT ÖZETİ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // RAF Bilgisi
            SummaryCard(
                title = "RAF Bilgisi",
                icon = Icons.Default.Inventory,
                onEdit = { onNavigateToStep(MobileNavigation.RafScan.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RAF No:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rafSeriNo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ürün Bilgileri
            SummaryCard(
                title = "Ürün Bilgileri",
                icon = Icons.Default.ShoppingCart,
                onEdit = { onNavigateToStep(MobileNavigation.UrunBarcode.route) }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ürün No
                    InfoRow(
                        label = "Ürün No:",
                        value = urunBilgileri.tasnifNo,
                        icon = Icons.Default.QrCode,
                        iconTint = Color(0xFF00897B)
                    )

                    // Açıklama
                    InfoRow(
                        label = "Açıklama:",
                        value = urunBilgileri.aciklama.ifEmpty { "-" },
                        icon = Icons.Default.Description
                    )

                    // Stok Birimi
                    val stokBirimi = stokBirimleri.find { it.id == urunBilgileri.stokBirimiId }
                    InfoRow(
                        label = "Stok Birimi:",
                        value = stokBirimi?.stokBirimiAdi ?: "-",
                        icon = Icons.Default.Inventory2
                    )

                    // Stok Birimi 2
                    urunBilgileri.stokBirimi2Id?.let { id ->
                        val stokBirimi2 = stokBirimleri.find { it.id == id }
                        stokBirimi2?.let {
                            InfoRow(
                                label = "Stok Birimi 2:",
                                value = it.stokBirimiAdi,
                                icon = Icons.Default.Inventory
                            )
                        }
                    }

                    // Boyutlar
                    val boyutlar = buildString {
                        urunBilgileri.en?.let { append("En: ${it}cm ") }
                        urunBilgileri.boy?.let { append("Boy: ${it}cm ") }
                        urunBilgileri.yukseklik?.let { append("Yük: ${it}cm") }
                    }

                    if (boyutlar.isNotEmpty()) {
                        InfoRow(
                            label = "Boyutlar:",
                            value = boyutlar,
                            icon = Icons.Default.Straighten
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fotoğraflar
            SummaryCard(
                title = "Fotoğraflar (${uploadedPhotos.size})",
                icon = Icons.Default.PhotoCamera,
                onEdit = { onNavigateToStep(MobileNavigation.PhotoCapture.route) }
            ) {
                if (uploadedPhotos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uploadedPhotos) { photo ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Gray)
                            ) {
                                AsyncImage(
                                    model = photo.localPath,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Upload status göstergesi
                                when (photo.uploadStatus) {
                                    UploadStatus.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Yüklendi",
                                            tint = Color.Green,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    UploadStatus.FAILED -> {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Hata",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Fotoğraf yok",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                if (uploadedPhotos.size < 4) {
                    Text(
                        text = "⚠️ Minimum 4 fotoğraf gerekli",
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Kaydet Butonu
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.canProceedToNext && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KAYDI TAMAMLA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Başarı ekranı - BURADA DEĞİŞTİRDİK (satır 279)
        // createdUrunSeriNo!! yerine null kontrolü eklendi
        createdUrunSeriNo?.let { urunNo ->
            SuccessScreen(
                urunSeriNo = urunNo,  // Artık güvenli, null değil
                rafNo = rafSeriNo,
                uploadedPhotoCount = uploadedPhotos.count { it.uploadStatus == UploadStatus.SUCCESS },
                onNewRegistration = {
                    viewModel.resetWorkflow()
                    onRegistrationComplete()
                }
            )
        } ?: run {
            // Eğer createdUrunSeriNo null ise hata göster
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ürün numarası oluşturulamadı!",
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.resetWorkflow()
                            onRegistrationComplete()
                        }
                    ) {
                        Text("Yeniden Dene")
                    }
                }
            }
        }
    }

    // Onay Dialog'u
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Kayıt Onayı") },
            text = { Text("Tüm bilgileri kontrol ettiniz mi? Kayıt işlemini onaylıyor musunuz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.completeRegistration()
                    }
                ) {
                    Text("Evet, Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kayıt işlemi yapılıyor...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
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

    // Success message
    successMessage?.let { message ->
        LaunchedEffect(message) {
            if (message.contains("Kayıt tamamlandı")) {
                // Başarı durumu zaten SuccessScreen ile gösteriliyor
            }
        }
    }
}

/**
 * Özet kartı komponenti
 */
@Composable
private fun SummaryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onEdit: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onEdit) {
                    Text("Düzenle")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            content()
        }
    }
}

/**
 * Bilgi satırı komponenti
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Başarı ekranı
 */
@Composable
private fun SuccessScreen(
    urunSeriNo: String,
    rafNo: String,
    uploadedPhotoCount: Int,
    onNewRegistration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Kayıt Başarıyla Tamamlandı!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ürün No:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = urunSeriNo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "RAF No: $rafNo",
                    fontSize = 14.sp
                )

                Text(
                    text = "Fotoğraf: $uploadedPhotoCount adet yüklendi",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNewRegistration,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "YENİ KAYIT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}