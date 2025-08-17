package com.example.stokkontrolveyonetimsistemi.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme

/**
 * Settings Screen Content - Clean Architecture
 * ✅ UPDATED: LocationActivity integration support
 *
 * Dosya Konumu: /presentation/settings/SettingsScreenContent.kt
 * Pattern: Pure UI components, ViewModel integration via parameter
 * Features: Password change + LocationActivity navigation
 */

/**
 * Main Settings Screen Content Composable
 * ✅ FIXED: Callback signature alignment with SettingsActivity.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onPasswordChangeSuccess: (String) -> Unit,
    onLocationChangeClick: () -> Unit,  // ✅ FIXED: LocationActivity navigation callback
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var activeScreen by remember { mutableStateOf(SettingsScreenType.MAIN) }

    // Observe ViewModel states
    val passwordChangeState by settingsViewModel.passwordChangeState.collectAsState()

    // ✅ FIXED: When expression with only 2 branches
    when (activeScreen) {
        SettingsScreenType.MAIN -> {
            SettingsMainScreen(
                onBackClick = onBackClick,
                onPasswordChangeClick = {
                    activeScreen = SettingsScreenType.PASSWORD_CHANGE
                },
                onLocationChangeClick = onLocationChangeClick  // ✅ FIXED: Direct delegation to Activity
            )
        }

        SettingsScreenType.PASSWORD_CHANGE -> {
            PasswordChangeScreen(
                passwordState = passwordChangeState,
                onBackClick = {
                    activeScreen = SettingsScreenType.MAIN
                },
                onPasswordChange = { current, new, confirm ->
                    settingsViewModel.changePassword(current, new, confirm)
                },
                onPasswordChangeSuccess = { message ->
                    onPasswordChangeSuccess(message)
                    activeScreen = SettingsScreenType.MAIN
                }
            )
        }

        // ✅ REMOVED: LOCATION_CHANGE branch - LocationActivity handles this now
    }
}

// ==========================================
// SETTINGS MAIN SCREEN
// ==========================================

/**
 * Settings Main Screen
 * Ana ayarlar menüsü
 */
@Composable
fun SettingsMainScreen(
    onBackClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onLocationChangeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Password Change Option
            SettingsOptionCard(
                icon = Icons.Default.Lock,
                title = "Şifre Değiştir",
                description = "Hesap şifrenizi güvenli bir şekilde değiştirin",
                onClick = onPasswordChangeClick
            )

            // Location Settings Option
            SettingsOptionCard(
                icon = Icons.Default.LocationOn,
                title = "Lokasyon Ayarları",
                description = "Bölge, il, ilçe ve depo bilgilerinizi güncelleyin",
                onClick = onLocationChangeClick  // ✅ FIXED: LocationActivity navigation
            )

            // Future options placeholder
            SettingsOptionCard(
                icon = Icons.Default.Notifications,
                title = "Bildirimler",
                description = "Bildirimleri yönetin ve tercihleri ayarlayın",
                onClick = { /* TODO: Implement notifications */ },
                enabled = false
            )

            SettingsOptionCard(
                icon = Icons.Default.Security,
                title = "Güvenlik",
                description = "Güvenlik ayarlarını yapılandırın",
                onClick = { /* TODO: Implement security */ },
                enabled = false
            )
        }
    }
}

// ==========================================
// PASSWORD CHANGE SCREEN
// ==========================================

/**
 * Password Change Screen
 * Şifre değiştirme ekranı
 */
@Composable
fun PasswordChangeScreen(
    passwordState: PasswordChangeState,
    onBackClick: () -> Unit,
    onPasswordChange: (String, String, String) -> Unit,
    onPasswordChangeSuccess: (String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Handle success state
    LaunchedEffect(passwordState) {
        if (passwordState is PasswordChangeState.Success) {
            onPasswordChangeSuccess(passwordState.message)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Şifre Değiştir",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Password Change Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Güvenlik",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Current Password Field
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mevcut Şifre") },
                    placeholder = { Text("Mevcut şifrenizi girin") },
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrentPassword) "Şifreyi gizle" else "Şifreyi göster"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = passwordState !is PasswordChangeState.Loading
                )

                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Yeni Şifre") },
                    placeholder = { Text("Yeni şifrenizi girin") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNewPassword) "Şifreyi gizle" else "Şifreyi göster"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = passwordState !is PasswordChangeState.Loading
                )

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Şifre Tekrarı") },
                    placeholder = { Text("Yeni şifrenizi tekrar girin") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) "Şifreyi gizle" else "Şifreyi göster"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (passwordState !is PasswordChangeState.Loading) {
                                onPasswordChange(currentPassword, newPassword, confirmPassword)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = passwordState !is PasswordChangeState.Loading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Change Password Button
                Button(
                    onClick = {
                        onPasswordChange(currentPassword, newPassword, confirmPassword)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = passwordState !is PasswordChangeState.Loading &&
                            currentPassword.isNotBlank() &&
                            newPassword.isNotBlank() &&
                            confirmPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (passwordState is PasswordChangeState.Loading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Değiştiriliyor...")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Şifreyi Değiştir")
                        }
                    }
                }

                // Error Display
                if (passwordState is PasswordChangeState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = passwordState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Password Requirements Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Şifre Gereksinimleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• En az 6 karakter olmalı\n• Mevcut şifrenizden farklı olmalı\n• Şifre tekrarı eşleşmeli",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ==========================================
// SETTINGS OPTION CARD COMPONENT
// ==========================================

/**
 * Settings Option Card
 * Ayarlar seçenekleri için reusable card component
 */
@Composable
fun SettingsOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==========================================
// SCREEN TYPES ENUM
// ==========================================

/**
 * Settings screen types
 * ✅ FIXED: LOCATION_CHANGE removed - LocationActivity handles location
 */
enum class SettingsScreenType {
    MAIN,
    PASSWORD_CHANGE
    // LOCATION_CHANGE removed - External LocationActivity navigation
}

// ==========================================
// STATE CLASSES - IMPORTED FROM SettingsViewModel.kt
// ==========================================

// PasswordChangeState is imported from SettingsViewModel.kt
// No duplicate definition needed here

// ==========================================
// PREVIEW FUNCTIONS
// ==========================================

@Preview(showBackground = true)
@Composable
fun SettingsMainScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        SettingsMainScreen(
            onBackClick = {},
            onPasswordChangeClick = {},
            onLocationChangeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsOptionCardPreview() {
    StokKontrolVeYonetimSistemiTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsOptionCard(
                icon = Icons.Default.Lock,
                title = "Şifre Değiştir",
                description = "Hesap şifrenizi güvenli bir şekilde değiştirin",
                onClick = {}
            )
        }
    }
}