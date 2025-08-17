package com.example.stokkontrolveyonetimsistemi.presentation.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.*
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Get current date in Turkish format
 */
private fun getCurrentDateText(): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
    return formatter.format(Date())
}

/**
 * Updated Dashboard Screen Content
 * Yeni tasarım: Lokasyon header + modüler kartlar + profil menüsü
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header: User Info + Location + Profile Icon
            item {
                DashboardHeader(
                    userSession = state.userInfo,
                    isLoading = state.isLoading,
                    onProfileClick = { onAction(DashboardAction.ShowUserMenu) }
                )
            }

            // Main Action Cards Section with Date
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ana İşlemler",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        text = getCurrentDateText(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Primary Action Cards (2x2 grid)
            item {
                PrimaryActionCards(
                    onRafNumarasiUret = { onAction(DashboardAction.NavigateToModule(DashboardModule.RAF_URETICI)) },
                    onUrunNumarasiUret = { onAction(DashboardAction.NavigateToModule(DashboardModule.URUN_URETICI)) },
                    onUrunEkle = { onAction(DashboardAction.NavigateToModule(DashboardModule.URUN_EKLE)) },
                    onBarkodTara = { onAction(DashboardAction.NavigateToModule(DashboardModule.SCANNER)) }
                )
            }

            // Quick Stats (if data is available)
            if (state.quickStats.hasActivityToday()) {
                item {
                    Text(
                        text = "Bugünkü Aktivite",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    TodayActivityCard(
                        stats = state.quickStats,
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onAction(DashboardAction.RefreshData) }
                    )
                }
            }
        }

        // Profile Menu Overlay (positioned over content)
        if (state.showUserMenu) {
            ProfileMenuDropdown(
                userSession = state.userInfo,
                onDismiss = { onAction(DashboardAction.HideUserMenu) },
                onSettingsClick = { onAction(DashboardAction.NavigateToModule(DashboardModule.SETTINGS)) },
                onLogoutClick = { onAction(DashboardAction.Logout) }
            )
        }

        // Error Snackbar at bottom
        if (state.showError) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                ErrorSnackbar(
                    message = state.errorMessage ?: "Bilinmeyen hata",
                    onDismiss = { onAction(DashboardAction.ClearError) }
                )
            }
        }
    }
}

/**
 * Dashboard Header with User Info and Location
 */
@Composable
fun DashboardHeader(
    userSession: UserSession?,
    isLoading: Boolean,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Info and Location
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    // App welcome message instead of username
                    Text(
                        text = "Stok Kontrol Sistemi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Location info
                    if (userSession?.currentLocation != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userSession.currentLocation.getFullLocationName(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lokasyon giriniz",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Profile Icon
            IconButton(
                onClick = onProfileClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Primary Action Cards (2x2 Grid)
 */
@Composable
fun PrimaryActionCards(
    onRafNumarasiUret: () -> Unit,
    onUrunNumarasiUret: () -> Unit,
    onUrunEkle: () -> Unit,
    onBarkodTara: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Raf Numarası Üret",
                icon = Icons.Default.Storage,
                color = Color(0xFF1976D2),
                onClick = onRafNumarasiUret,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "Ürün Numarası Üret",
                icon = Icons.Default.QrCode,
                color = Color(0xFF388E3C),
                onClick = onUrunNumarasiUret,
                modifier = Modifier.weight(1f)
            )
        }

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Ürün Ekle",
                icon = Icons.Default.Add,
                color = Color(0xFFE64A19),
                onClick = onUrunEkle,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = "Barkod Tara",
                icon = Icons.Default.QrCodeScanner,
                color = Color(0xFF7B1FA2),
                onClick = onBarkodTara,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Action Card Component
 */
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Today Activity Card (Compact)
 */
@Composable
fun TodayActivityCard(
    stats: QuickStats,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugünkü İşlemler",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActivityStatItem(
                    label = "Taranan",
                    value = stats.todayScanned.toString(),
                    icon = Icons.Default.QrCodeScanner
                )
                ActivityStatItem(
                    label = "Ürün",
                    value = stats.todayProducts.toString(),
                    icon = Icons.Default.Inventory
                )
                ActivityStatItem(
                    label = "Raf",
                    value = stats.todayShelf.toString(),
                    icon = Icons.Default.Storage
                )
            }
        }
    }
}

/**
 * Activity Stat Item
 */
@Composable
fun ActivityStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Modern Profile Menu as a Card (positioned below profile icon)
 */
@Composable
fun ProfileMenuDropdown(
    userSession: UserSession?,
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Backdrop to dismiss menu
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable { onDismiss() }
    ) {
        // Profile menu card positioned at top right
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(top = 80.dp, end = 16.dp)
                .align(Alignment.TopEnd),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // User info header
                userSession?.let { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        // User avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = user.getDisplayName(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user.getRoleDisplayName(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Settings menu item
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Ayarlar",
                    subtitle = "Şifre ve lokasyon değiştir",
                    onClick = {
                        onDismiss()
                        onSettingsClick()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ LOGOUT MENU ITEM - ENHANCED LOGGING
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Çıkış Yap",
                    subtitle = "Güvenli çıkış",
                    iconTint = MaterialTheme.colorScheme.error,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        // ✅ DEBUG: Log button click
                        Log.d("DashboardScreenContent", "🔴 LOGOUT MENU ITEM CLICKED")
                        onDismiss()
                        showLogoutConfirm = true
                        Log.d("DashboardScreenContent", "🔴 LOGOUT CONFIRMATION DIALOG SHOWN")
                    }
                )
            }
        }
    }

    // ✅ LOGOUT CONFIRMATION DIALOG - ENHANCED LOGGING
    if (showLogoutConfirm) {
        Log.d("DashboardScreenContent", "🔴 LOGOUT CONFIRMATION DIALOG RENDERING")

        AlertDialog(
            onDismissRequest = {
                Log.d("DashboardScreenContent", "🔴 LOGOUT DIALOG DISMISSED")
                showLogoutConfirm = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Çıkış Onayı")
                }
            },
            text = {
                Text("Çıkış yapmak istediğinizden emin misiniz?\nTüm oturum verileriniz silinecek.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // ✅ CRITICAL DEBUG: Maximum logging
                        Log.d("DashboardScreenContent", "🚨 LOGOUT CONFIRM BUTTON CLICKED!")
                        Log.d("DashboardScreenContent", "🚨 CALLING onLogoutClick() CALLBACK")

                        showLogoutConfirm = false

                        // ✅ CRITICAL: Call logout callback
                        onLogoutClick()

                        Log.d("DashboardScreenContent", "🚨 onLogoutClick() CALLBACK EXECUTED")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Çıkış Yap")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Log.d("DashboardScreenContent", "🔴 LOGOUT DIALOG CANCELLED")
                        showLogoutConfirm = false
                    }
                ) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Profile Menu Item Component
 */
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Error Snackbar
 */
@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ==========================================
// PREVIEW FUNCTIONS
// ==========================================

@Preview(showBackground = true)
@Composable
fun DashboardHeaderPreview() {
    StokKontrolVeYonetimSistemiTheme {
        DashboardHeader(
            userSession = null,
            isLoading = false,
            onProfileClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrimaryActionCardsPreview() {
    StokKontrolVeYonetimSistemiTheme {
        PrimaryActionCards(
            onRafNumarasiUret = {},
            onUrunNumarasiUret = {},
            onUrunEkle = {},
            onBarkodTara = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ActionCardPreview() {
    StokKontrolVeYonetimSistemiTheme {
        ActionCard(
            title = "Raf Numarası Üret",
            icon = Icons.Default.Storage,
            color = Color(0xFF1976D2),
            onClick = {}
        )
    }
}