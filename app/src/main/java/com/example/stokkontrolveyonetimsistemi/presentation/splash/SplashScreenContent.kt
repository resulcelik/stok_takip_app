package com.example.stokkontrolveyonetimsistemi.presentation.auth.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme

/**
 * Professional splash screen content
 * Modern gradient tasarım ile corporate splash screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreenContent(
    isLoading: Boolean = true,
    statusMessage: String = "Uygulama başlatılıyor...",
    appVersion: String = "1.0.0",
    companyName: String = "KeyCyte Technology"
) {
    // Animation states
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var progressVisible by remember { mutableStateOf(false) }

    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    // Gradient colors
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondary
    )

    // Animation triggers
    LaunchedEffect(Unit) {
        logoVisible = true
        kotlinx.coroutines.delay(300)
        textVisible = true
        kotlinx.coroutines.delay(500)
        progressVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {

            // ==========================================
            // LOGO SECTION
            // ==========================================
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 800)
                ) + scaleIn(
                    initialScale = 0.3f,
                    animationSpec = tween(durationMillis = 800)
                )
            ) {
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // APP TITLE SECTION
            // ==========================================
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 300)
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(durationMillis = 600, delayMillis = 300)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "STOK KONTROL",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Mobil Yönetim Sistemi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ==========================================
            // STATUS & PROGRESS SECTION
            // ==========================================
            AnimatedVisibility(
                visible = progressVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 500, delayMillis = 800)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(280.dp)
                ) {
                    // Status message
                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Loading progress indicator
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // BOTTOM INFO SECTION
        // ==========================================
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 600, delayMillis = 1000)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Text(
                    text = companyName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "v$appVersion",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Splash screen with pulsing logo animation
 * Alternative animated splash design
 */
@Composable
fun AnimatedSplashScreen(
    isLoading: Boolean = true,
    statusMessage: String = "Yükleniyor...",
    appVersion: String = "1.0.0",
    companyName: String = "KeyCyte Technology"
) {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = pulseAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App title
            Text(
                text = "STOK KONTROL",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mobil Sistem",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Status message
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
        }

        // Bottom info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = companyName,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
            Text(
                text = "v$appVersion",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            )
        }
    }
}

// ==========================================
// PREVIEW FUNCTIONS
// ==========================================

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        SplashScreenContent(
            isLoading = true,
            statusMessage = "Güvenlik kontrolü...",
            appVersion = "1.0.0",
            companyName = "KeyCyte Technology"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedSplashScreenPreview() {
    StokKontrolVeYonetimSistemiTheme {
        AnimatedSplashScreen(
            isLoading = true,
            statusMessage = "Otomatik giriş kontrol ediliyor...",
            appVersion = "1.0.0",
            companyName = "KeyCyte Technology"
        )
    }
}