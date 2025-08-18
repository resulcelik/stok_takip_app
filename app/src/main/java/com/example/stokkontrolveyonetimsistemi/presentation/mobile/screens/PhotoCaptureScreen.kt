package com.example.stokkontrolveyonetimsistemi.presentation.mobile.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.MobileRegistrationViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Photo Capture Screen
 * Fotoğraf çekme ekranı - Minimum 4 fotoğraf
 * Fotoğraflar local'de saklanır, kayıt sonrası upload edilir
 */
@Composable
fun PhotoCaptureScreen(
    viewModel: MobileRegistrationViewModel,
    onNavigateToReview: () -> Unit
) {
    // State'leri observe et - ViewModel'deki yeni yapıya uygun
    val uploadedPhotos by viewModel.uploadedPhotos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val rafSeriNo by viewModel.rafSeriNo.collectAsState()
    val urunBilgileri by viewModel.urunBilgileri.collectAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()

    // Camera permission
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera state
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Üst bilgi paneli
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📸 FOTOĞRAF ÇEKİMİ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Flash butonu
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                            imageCapture?.flashMode = flashMode
                        }
                    ) {
                        Icon(
                            imageVector = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                else -> Icons.Default.FlashAuto
                            },
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }
                }

                // Bilgi metni
                Text(
                    text = "📍 RAF: $rafSeriNo | 📦 Ürün: ${urunBilgileri.tasnifNo}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (hasCameraPermission) {
            // Camera Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                imageCapture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .setFlashMode(flashMode)
                                    .build()

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Permission request
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Kamera İzni Gerekli",
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("İzin Ver")
                    }
                }
            }
        }

        // Alt panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Fotoğraf sayacı
                val photoCount = uploadedPhotos.size
                val minPhotoCount = 4

                Text(
                    text = "Çekilen Fotoğraflar ($photoCount/$minPhotoCount)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (photoCount >= minPhotoCount) Color.Green else Color.White
                )

                if (photoCount < minPhotoCount) {
                    val remaining = minPhotoCount - photoCount
                    Text(
                        text = "⚠️ $remaining fotoğraf daha gerekli",
                        fontSize = 12.sp,
                        color = Color(0xFFFFAB00),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fotoğraf grid
                if (uploadedPhotos.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uploadedPhotos) { photo ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Gray)
                            ) {
                                AsyncImage(
                                    model = photo.localPath,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Upload durumu göstergesi
                                when (photo.uploadStatus) {
                                    com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.UploadStatus.UPLOADING -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                    com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.UploadStatus.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Yüklendi",
                                            tint = Color.Green,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(2.dp)
                                                .size(16.dp)
                                        )
                                    }
                                    com.example.stokkontrolveyonetimsistemi.presentation.viewmodel.UploadStatus.FAILED -> {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Hata",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(2.dp)
                                                .size(16.dp)
                                        )
                                    }
                                    else -> {}
                                }

                                // Silme butonu
                                IconButton(
                                    onClick = {
                                        viewModel.removePhoto(photo.localPath)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Sil",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Butonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fotoğraf çek butonu
                    Button(
                        onClick = {
                            val photoFile = File(
                                context.getExternalFilesDir(null),
                                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                                    .format(Date()) + ".jpg"
                            )

                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCapture?.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        // addPhoto metodunu çağır (uploadPhoto yerine)
                                        viewModel.addPhoto(photoFile)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                        viewModel.clearError()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = hasCameraPermission && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FOTOĞRAF ÇEK")
                    }

                    // Devam et butonu
                    Button(
                        onClick = onNavigateToReview,
                        modifier = Modifier.weight(1f),
                        enabled = photoCount >= minPhotoCount && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (photoCount >= minPhotoCount)
                                MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text("DEVAM ET")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    // Loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    // Error message handling with Toast
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearError()
        }
    }

    // Success message handling with Toast
    successMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearSuccess()
        }
    }
}