package com.example.stokkontrolveyonetimsistemi.presentation.urun

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.data.model.product.UrunEtiketUiState
import com.example.stokkontrolveyonetimsistemi.data.model.product.PrinterState
import kotlinx.coroutines.delay

/**
 * ÜRÜN Etiket Screen - Modern ve Güzel Tasarım
 * Glassmorphism, animasyonlar ve modern UI elementleri
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UrunEtiketScreen(
    uiState: UrunEtiketUiState,
    printerState: PrinterState,
    onAdetChanged: (String) -> Unit,
    onGenerateClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onClearError: () -> Unit
) {
    var adetInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3C72),
                        Color(0xFF2A5298),
                        Color(0xFF7E8BA3)
                    )
                )
            )
    ) {
        // Animated background shapes
        AnimatedBackgroundShapes()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                ModernTopBar(
                    onBackClick = onBackClick,
                    printerConnected = printerState.isConnected
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Printer Durumu Kartı
                PrinterStatusCard(isConnected = printerState.isConnected)

                // Ana İçerik Kartı
                GlassmorphicCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Başlık ve İkon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedIcon(
                                icon = Icons.Default.QrCode,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Etiket Adedi",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Adet Input
                        ModernTextField(
                            value = adetInput,
                            onValueChange = {
                                adetInput = it
                                onAdetChanged(it)
                            },
                            enabled = !uiState.isLoading && !printerState.isPrinting,
                            isError = adetInput.isNotEmpty() &&
                                    (adetInput.toIntOrNull() ?: 0) !in 1..100
                        )

                        // Hızlı Seçim Butonları
                        QuickSelectButtons(
                            enabled = !uiState.isLoading && !printerState.isPrinting,
                            onSelect = {
                                adetInput = it
                                onAdetChanged(it)
                            }
                        )

                        // Üret Butonu
                        AnimatedGenerateButton(
                            isValid = adetInput.isNotEmpty() &&
                                    (adetInput.toIntOrNull() ?: 0) in 1..100,
                            isLoading = uiState.isLoading,
                            isPrinting = printerState.isPrinting,
                            onClick = {
                                val adet = adetInput.toIntOrNull() ?: 0
                                if (adet in 1..100) {
                                    onGenerateClick(adet)
                                }
                            }
                        )
                    }
                }

                // Üretilen Etiketler
                AnimatedVisibility(
                    visible = uiState.lastGeneratedNumbers.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    GeneratedLabelsCard(
                        urunNumbers = uiState.lastGeneratedNumbers,
                        isPrinting = printerState.isPrinting,
                        printProgress = printerState.getProgressPercentage()
                    )
                }

                // Hata Mesajı
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = slideInHorizontally() + fadeIn(),
                    exit = slideOutHorizontally() + fadeOut()
                ) {
                    ErrorCard(
                        message = uiState.error ?: "",
                        onDismiss = onClearError
                    )
                }
            }
        }
    }
}

/**
 * Printer Status Card
 */
@Composable
private fun PrinterStatusCard(isConnected: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isConnected) Color(0xFF4CAF50).copy(alpha = 0.3f)
                else Color(0xFFFF5252).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                Color(0xFF4CAF50).copy(alpha = 0.2f)
            else
                Color(0xFFFF5252).copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isConnected)
                Color(0xFF4CAF50).copy(alpha = 0.5f)
            else
                Color(0xFFFF5252).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "Printer Bağlı" else "Printer Bağlı Değil",
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
                if (isConnected) {
                    Text(
                        text = "TSC Alpha-3R",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            if (isConnected) {
                Badge(
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text("Hazır", color = Color.White)
                }
            }
        }
    }
}

/**
 * Animated Background Shapes
 */
@Composable
private fun AnimatedBackgroundShapes() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                radius = 300.dp.toPx(),
                center = center.copy(x = size.width * 0.2f, y = size.height * 0.3f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                radius = 200.dp.toPx(),
                center = center.copy(x = size.width * 0.8f, y = size.height * 0.6f)
            )
        }
    }
}

/**
 * Modern Top Bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    onBackClick: () -> Unit,
    printerConnected: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text = "ÜRÜN Etiket Üretimi",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }
        },
        actions = {
            // Printer ikonu sağ üstte
            Icon(
                imageVector = if (printerConnected) Icons.Default.Print else Icons.Default.PrintDisabled,
                contentDescription = null,
                tint = if (printerConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * Glassmorphic Card
 */
@Composable
private fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.3f)
        )
    ) {
        content()
    }
}

/**
 * Modern TextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                onValueChange(newValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text("Adet (1-100)", color = Color.White.copy(alpha = 0.9f)) },
        placeholder = { Text("Örn: 10", color = Color.White.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(
                Icons.Default.Numbers,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.7f)
            )
        },
        isError = isError,
        supportingText = if (isError) {
            { Text("1-100 arası bir değer girin", color = MaterialTheme.colorScheme.error) }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
            focusedBorderColor = Color.White.copy(alpha = 0.8f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

/**
 * Quick Select Buttons
 */
@Composable
private fun QuickSelectButtons(
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Hızlı Seçim",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 4.dp)
        )

        // İlk satır: 5, 10, 25, 50
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("5", "10", "25", "50").forEach { value ->
                QuickButton(
                    text = value,
                    enabled = enabled,
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // İkinci satır: 100 - Ortalanmış
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            QuickButton(
                text = "100",
                enabled = enabled,
                onClick = { onSelect("100") },
                modifier = Modifier.width(95.dp)
            )
        }
    }
}

/**
 * Quick Button
 */
@Composable
private fun QuickButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(if (isPressed) 0.95f else 1f),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

/**
 * Animated Generate Button
 */
@Composable
private fun AnimatedGenerateButton(
    isValid: Boolean,
    isLoading: Boolean,
    isPrinting: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isValid && !isLoading && !isPrinting) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = isValid && !isLoading && !isPrinting,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isValid) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Gray.copy(alpha = 0.5f)
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        AnimatedContent(
            targetState = when {
                isLoading -> "loading"
                isPrinting -> "printing"
                else -> "generate"
            },
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "buttonContent"
        ) { state ->
            when (state) {
                "loading" -> LoadingContent()
                "printing" -> PrintingContent()
                else -> GenerateContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Üretiliyor...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PrintingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Print,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Yazdırılıyor...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun GenerateContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "ÜRÜN Etiketi Üret ve Yazdır",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Animated Icon
 */
@Composable
private fun AnimatedIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(48.dp)
            .scale(scale)
    )
}

/**
 * Generated Labels Card
 */
@Composable
private fun GeneratedLabelsCard(
    urunNumbers: List<String>,
    isPrinting: Boolean,
    printProgress: Int
) {
    GlassmorphicCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPrinting) Icons.Default.Print else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPrinting) Color(0xFFFFA726) else Color(0xFF66BB6A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPrinting) "Yazdırılıyor..." else "Üretim Tamamlandı",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "${urunNumbers.size} Adet",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress Bar
            if (isPrinting) {
                LinearProgressIndicator(
                    progress = printProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFFA726),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            // Numbers Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ÜRÜN için tek numara göster
                    LabelRow("ÜRÜN Numarası:", urunNumbers.firstOrNull() ?: "")
                    LabelRow("Yazdırılacak Adet:", "${urunNumbers.size}")

                    if (urunNumbers.size > 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Text(
                            "Aynı numara ${urunNumbers.size} kez yazdırılacak",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

/**
 * Error Card
 */
@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}