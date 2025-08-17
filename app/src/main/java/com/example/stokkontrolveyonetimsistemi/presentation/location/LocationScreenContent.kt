package com.example.stokkontrolveyonetimsistemi.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme

/**
 * Location Screen Content
 * Modern Material3 cascade dropdown UI
 *
 * Dosya Konumu: /presentation/location/LocationScreenContent.kt
 * Features: 4-level cascade, loading states, error handling, validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreenContent(
    state: LocationSelectionState,
    onBolgeSelected: (BolgeDto) -> Unit,
    onIlSelected: (IlDto) -> Unit,
    onIlceSelected: (IlceDto) -> Unit,
    onDepoSelected: (DepoDto) -> Unit,
    onSaveLocation: () -> Unit,
    onResetSelection: () -> Unit,
    onBackPressed: () -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ==========================================
        // HEADER SECTION
        // ==========================================
        LocationHeader(
            onBackPressed = onBackPressed,
            selectionProgress = calculateSelectionProgress(state)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // ERROR HANDLING
        // ==========================================
        if (state.errorMessage != null) {
            ErrorCard(
                message = state.errorMessage,
                onDismiss = onErrorDismissed
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==========================================
        // CASCADE DROPDOWN SECTION
        // ==========================================
        CascadeDropdownSection(
            state = state,
            onBolgeSelected = onBolgeSelected,
            onIlSelected = onIlSelected,
            onIlceSelected = onIlceSelected,
            onDepoSelected = onDepoSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // SELECTION SUMMARY
        // ==========================================
        if (state.isSelectionComplete()) {
            SelectionSummaryCard(
                summary = state.getSelectionSummary() ?: ""
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==========================================
        // ACTION BUTTONS
        // ==========================================
        ActionButtonsSection(
            isSelectionComplete = state.isSelectionComplete(),
            isLoading = state.isLoading,
            state = state,
            onSaveLocation = onSaveLocation,
            onResetSelection = onResetSelection
        )

        // Bottom padding for better scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==========================================
// HEADER COMPONENT
// ==========================================

@Composable
private fun LocationHeader(
    onBackPressed: () -> Unit,
    selectionProgress: Float
) {
    Column {
        // Header row with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Lokasyon Seçimi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Progress indicator
        if (selectionProgress > 0f) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = selectionProgress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

// ==========================================
// ERROR CARD COMPONENT
// ==========================================

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==========================================
// CASCADE DROPDOWN SECTION
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CascadeDropdownSection(
    state: LocationSelectionState,
    onBolgeSelected: (BolgeDto) -> Unit,
    onIlSelected: (IlDto) -> Unit,
    onIlceSelected: (IlceDto) -> Unit,
    onDepoSelected: (DepoDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Lokasyon Hiyerarşisi",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1st Level: Bölge Dropdown
            LocationDropdown(
                label = "Bölge Seçiniz",
                items = state.availableBolgeler,
                selectedItem = state.selectedBolge,
                onItemSelected = onBolgeSelected,
                enabled = true,
                isLoading = state.isLoading && state.availableBolgeler.isEmpty(),
                itemDisplayText = { it.bolgeAdi }
            )

            // 2nd Level: İl Dropdown
            LocationDropdown(
                label = "İl Seçiniz",
                items = state.availableIller,
                selectedItem = state.selectedIl,
                onItemSelected = onIlSelected,
                enabled = state.canSelectIl() && !state.isLoading,
                isLoading = state.isLoading && state.selectedBolge != null && state.availableIller.isEmpty(),
                itemDisplayText = { it.ilAdi }
            )

            // 3rd Level: İlçe Dropdown
            LocationDropdown(
                label = "İlçe Seçiniz",
                items = state.availableIlceler,
                selectedItem = state.selectedIlce,
                onItemSelected = onIlceSelected,
                enabled = state.canSelectIlce() && !state.isLoading,
                isLoading = state.isLoading && state.selectedIl != null && state.availableIlceler.isEmpty(),
                itemDisplayText = { it.ilceAdi }
            )

            // 4th Level: Depo Dropdown
            LocationDropdown(
                label = "Depo Seçiniz",
                items = state.availableDepolar,
                selectedItem = state.selectedDepo,
                onItemSelected = onDepoSelected,
                enabled = state.canSelectDepo() && !state.isLoading,
                isLoading = state.isLoading && state.selectedIlce != null && state.availableDepolar.isEmpty(),
                itemDisplayText = { it.depoAdi }
            )
        }
    }
}

// ==========================================
// GENERIC DROPDOWN COMPONENT
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LocationDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    itemDisplayText: (T) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Dropdown label
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Dropdown box
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it && enabled && !isLoading }
        ) {
            OutlinedTextField(
                value = selectedItem?.let(itemDisplayText) ?: "",
                onValueChange = { },
                readOnly = true,
                placeholder = {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Yükleniyor...")
                        }
                    } else {
                        Text(
                            text = if (enabled) label else "Önce üst seviyeyi seçiniz",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                enabled = enabled && !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            // Dropdown menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemDisplayText(item),
                                fontSize = 16.sp
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }

                // Empty state
                if (items.isEmpty() && enabled && !isLoading) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Veri bulunamadı",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}

// ==========================================
// SELECTION SUMMARY CARD
// ==========================================

@Composable
private fun SelectionSummaryCard(
    summary: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Seçilen Lokasyon",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==========================================
// ACTION BUTTONS SECTION
// ==========================================

@Composable
private fun ActionButtonsSection(
    isSelectionComplete: Boolean,
    isLoading: Boolean,
    state: LocationSelectionState,
    onSaveLocation: () -> Unit,
    onResetSelection: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Save button
        Button(
            onClick = onSaveLocation,
            enabled = isSelectionComplete && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kaydediliyor...")
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOKASYONU KAYDET",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Reset button
        OutlinedButton(
            onClick = onResetSelection,
            enabled = !isLoading && (isSelectionComplete || hasAnySelection(state)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SEÇİMLERİ SIFIRLA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==========================================
// HELPER FUNCTIONS
// ==========================================

/**
 * Calculate selection progress for UI
 */
private fun calculateSelectionProgress(state: LocationSelectionState): Float {
    var progress = 0f
    if (state.selectedBolge != null) progress += 0.25f
    if (state.selectedIl != null) progress += 0.25f
    if (state.selectedIlce != null) progress += 0.25f
    if (state.selectedDepo != null) progress += 0.25f
    return progress
}

/**
 * Check if user has made any selection
 */
private fun hasAnySelection(state: LocationSelectionState): Boolean {
    return state.selectedBolge != null ||
            state.selectedIl != null ||
            state.selectedIlce != null ||
            state.selectedDepo != null
}

// ==========================================
// PREVIEW COMPONENTS (Backend'den gelecek gerçek data için hazırlık)
// ==========================================

@Preview(showBackground = true)
@Composable
private fun LocationScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        LocationScreenContent(
            state = LocationSelectionState(
                // Sample data - gerçek data backend'den gelecek
                availableBolgeler = listOf(
                    BolgeDto(1, "Marmara Bölgesi"),
                    BolgeDto(2, "Ege Bölgesi"),
                    BolgeDto(3, "Akdeniz Bölgesi")
                ),
                selectedBolge = BolgeDto(1, "Marmara Bölgesi"),
                availableIller = listOf(
                    IlDto(34, "İstanbul", 1),
                    IlDto(6, "Ankara", 1),
                    IlDto(35, "İzmir", 1)
                ),
                isLoading = false
            ),
            onBolgeSelected = { },
            onIlSelected = { },
            onIlceSelected = { },
            onDepoSelected = { },
            onSaveLocation = { },
            onResetSelection = { },
            onBackPressed = { },
            onErrorDismissed = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationScreenLoadingPreview() {
    StokKontrolVeYonetimSistemiTheme {
        LocationScreenContent(
            state = LocationSelectionState(
                isLoading = true,
                availableBolgeler = emptyList()
            ),
            onBolgeSelected = { },
            onIlSelected = { },
            onIlceSelected = { },
            onDepoSelected = { },
            onSaveLocation = { },
            onResetSelection = { },
            onBackPressed = { },
            onErrorDismissed = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationScreenErrorPreview() {
    StokKontrolVeYonetimSistemiTheme {
        LocationScreenContent(
            state = LocationSelectionState(
                errorMessage = "Network bağlantısı hatası. Lütfen tekrar deneyiniz.",
                availableBolgeler = emptyList()
            ),
            onBolgeSelected = { },
            onIlSelected = { },
            onIlceSelected = { },
            onDepoSelected = { },
            onSaveLocation = { },
            onResetSelection = { },
            onBackPressed = { },
            onErrorDismissed = { }
        )
    }
}