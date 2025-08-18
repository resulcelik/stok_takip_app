package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel

/**
 * Location Check Screen
 * Lokasyon kontrolü ve session yönetimi
 */
@Composable
fun LocationCheckScreen(
    viewModel: MobileRegistrationViewModel,
    onNavigateToRafScan: () -> Unit,
    onOpenLocationActivity: () -> Unit
) {
    // State'leri observe et
    val userSession by viewModel.userSession.observeAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()

    // İlk yüklemede session kontrolü yap
    LaunchedEffect(Unit) {
        viewModel.checkUserSession()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "📍 LOKASYON KONTROLÜ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Lokasyon durumu
        val hasLocation = viewModel.hasValidLocation

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasLocation) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasLocation) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasLocation) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (hasLocation) "Lokasyon Seçildi" else "Lokasyon Seçilmedi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasLocation) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            }
        }

        // Lokasyon bilgileri
        userSession?.let { session ->
            if (session.selectedDepoId != null && session.selectedDepoId > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Bölge
                        LocationInfoRow(
                            icon = Icons.Default.Place,
                            label = "Bölge:",
                            value = session.selectedBolgeAdi ?: "-"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Depo
                        LocationInfoRow(
                            icon = Icons.Default.Store,
                            label = "Depo:",
                            value = session.selectedDepoAdi ?: "-"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Depo ID
                        LocationInfoRow(
                            icon = Icons.Default.Tag,
                            label = "Depo ID:",
                            value = session.selectedDepoId.toString()
                        )

                        // Session süresi
                        val remainingMinutes = session.sessionRemainingMinutes ?: 0
                        if (remainingMinutes > 0) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            LocationInfoRow(
                                icon = Icons.Default.Timer,
                                label = "Session:",
                                value = "$remainingMinutes dakika kaldı",
                                valueColor = if (remainingMinutes < 5) Color.Red else Color.Black
                            )
                        }

                        // Lokasyon detayı
                        session.selectedLokasyonDetay?.let { detail ->
                            if (detail.isNotEmpty()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = detail,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Lokasyon seçilmemiş uyarısı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Lokasyon Seçimi Gerekli",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ürün kaydı için önce depo lokasyonu seçmelisiniz",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Butonlar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Lokasyon Seç/Değiştir
            Button(
                onClick = onOpenLocationActivity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasLocation) "Lokasyonu Değiştir" else "Lokasyon Seç",
                    fontSize = 16.sp
                )
            }

            // Yenile
            OutlinedButton(
                onClick = { viewModel.checkUserSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Yenile")
            }

            // Devam Et
            Button(
                onClick = onNavigateToRafScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = hasLocation && !isLoading,
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

    // Error message
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Toast veya Snackbar göster
            viewModel.clearError()
        }
    }

    // Success message
    successMessage?.let { message ->
        LaunchedEffect(message) {
            // Toast veya Snackbar göster
            viewModel.clearSuccess()
        }
    }
}

/**
 * Lokasyon bilgi satırı komponenti
 */
@Composable
private fun LocationInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}