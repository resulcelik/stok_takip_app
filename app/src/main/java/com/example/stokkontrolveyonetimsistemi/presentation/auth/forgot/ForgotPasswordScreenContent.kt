package com.example.stokkontrolveyonetimsistemi.presentation.auth.forgot

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stokkontrolveyonetimsistemi.data.model.auth.AuthState
import com.example.stokkontrolveyonetimsistemi.ui.theme.StokKontrolVeYonetimSistemiTheme

/**
 * Main Forgot Password Screen Content
 * Multi-step password reset flow with smooth animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreenContent(
    uiState: ForgotPasswordUiState,
    resetState: AuthState,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSendResetEmail: () -> Unit,
    onVerifyCode: () -> Unit,
    onResetPassword: () -> Unit,
    onBackToLogin: () -> Unit,
    onStepChange: (Int) -> Unit,
    onClearErrors: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.surface
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ==========================================
            // HEADER SECTION
            // ==========================================
            ForgotPasswordHeader(
                step = uiState.currentStep,
                title = uiState.getStepTitle(),
                description = uiState.getStepDescription(),
                onBackToLogin = onBackToLogin
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // PROGRESS INDICATOR
            // ==========================================
            ForgotPasswordProgressIndicator(
                currentStep = uiState.currentStep,
                totalSteps = 4
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // STEP CONTENT
            // ==========================================
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(600)
                    ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(300)
                    )
                },
                label = "step_content"
            ) { step ->
                when (step) {
                    1 -> EmailInputStep(
                        email = uiState.email,
                        emailError = uiState.emailError,
                        generalError = uiState.generalError,
                        resetState = resetState,
                        onEmailChange = onEmailChange,
                        onSendResetEmail = onSendResetEmail,
                        canProceed = uiState.canProceed()
                    )

                    2 -> CodeVerificationStep(
                        code = uiState.verificationCode,
                        email = uiState.email,
                        codeError = uiState.codeError,
                        generalError = uiState.generalError,
                        resetState = resetState,
                        onCodeChange = onCodeChange,
                        onVerifyCode = onVerifyCode,
                        onResendCode = onSendResetEmail,
                        canProceed = uiState.canProceed()
                    )

                    3 -> NewPasswordStep(
                        newPassword = uiState.newPassword,
                        confirmPassword = uiState.confirmPassword,
                        isPasswordVisible = uiState.isPasswordVisible,
                        isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
                        passwordError = uiState.passwordError,
                        confirmPasswordError = uiState.confirmPasswordError,
                        generalError = uiState.generalError,
                        resetState = resetState,
                        onNewPasswordChange = onNewPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                        onConfirmPasswordVisibilityToggle = onConfirmPasswordVisibilityToggle,
                        onResetPassword = onResetPassword,
                        canProceed = uiState.canProceed()
                    )

                    4 -> SuccessStep(
                        onBackToLogin = onBackToLogin
                    )
                }
            }
        }
    }
}

/**
 * Header with title and back button
 */
@Composable
fun ForgotPasswordHeader(
    step: Int,
    title: String,
    description: String,
    onBackToLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button and title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackToLogin,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Spacer for alignment
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Progress indicator for multi-step flow
 */
@Composable
fun ForgotPasswordProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            // Step circle
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = when {
                    i < currentStep -> MaterialTheme.colorScheme.primary
                    i == currentStep -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (i < currentStep) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$i",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (i == currentStep) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Connector line (except for last step)
            if (i < totalSteps) {
                Divider(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp),
                    color = if (i < currentStep) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Step 1: Email Input
 */
@Composable
fun EmailInputStep(
    email: String,
    emailError: String?,
    generalError: String?,
    resetState: AuthState,
    onEmailChange: (String) -> Unit,
    onSendResetEmail: () -> Unit,
    canProceed: Boolean
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email input field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-posta Adresi") },
                placeholder = { Text("ornek@email.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "E-posta"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is AuthState.Loading,
                isError = emailError != null,
                supportingText = {
                    emailError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canProceed) {
                            onSendResetEmail()
                        }
                    }
                ),
                singleLine = true
            )

            // General error
            generalError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Send email button
            Button(
                onClick = onSendResetEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canProceed,
                shape = RoundedCornerShape(12.dp)
            ) {
                when (val currentResetState = resetState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("E-posta gönderiliyor...")
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOĞRULAMA KODU GÖNDER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 2: Code Verification
 */
@Composable
fun CodeVerificationStep(
    code: String,
    email: String,
    codeError: String?,
    generalError: String?,
    resetState: AuthState,
    onCodeChange: (String) -> Unit,
    onVerifyCode: () -> Unit,
    onResendCode: () -> Unit,
    canProceed: Boolean
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email info
            Text(
                text = "Kod gönderildi: ${email.take(3)}***${email.takeLast(4)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Code input field
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("6 Haneli Doğrulama Kodu") },
                placeholder = { Text("123456") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Pin,
                        contentDescription = "Kod"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is AuthState.Loading,
                isError = codeError != null,
                supportingText = {
                    codeError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canProceed) {
                            onVerifyCode()
                        }
                    }
                ),
                singleLine = true
            )

            // General error
            generalError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Verify button
            Button(
                onClick = onVerifyCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canProceed,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KODU DOĞRULA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Resend code button
            TextButton(
                onClick = onResendCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is AuthState.Loading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kodu tekrar gönder")
            }
        }
    }
}

/**
 * Step 3: New Password Input
 */
@Composable
fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    passwordError: String?,
    confirmPasswordError: String?,
    generalError: String?,
    resetState: AuthState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onResetPassword: () -> Unit,
    canProceed: Boolean
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New password field
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("Yeni Şifre") },
                placeholder = { Text("••••••••") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Şifre"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (isPasswordVisible) {
                                "Şifreyi gizle"
                            } else {
                                "Şifreyi göster"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is AuthState.Loading,
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true
            )

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Şifre Tekrarı") },
                placeholder = { Text("••••••••") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Şifre tekrarı"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (isConfirmPasswordVisible) {
                                "Şifreyi gizle"
                            } else {
                                "Şifreyi göster"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is AuthState.Loading,
                isError = confirmPasswordError != null,
                supportingText = {
                    confirmPasswordError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                visualTransformation = if (isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canProceed) {
                            onResetPassword()
                        }
                    }
                ),
                singleLine = true
            )

            // General error
            generalError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Reset password button
            Button(
                onClick = onResetPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canProceed,
                shape = RoundedCornerShape(12.dp)
            ) {
                when (val currentResetState = resetState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Şifre sıfırlanıyor...")
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ŞİFREYİ SIFIRLA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 4: Success Step
 */
@Composable
fun SuccessStep(
    onBackToLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Success title
            Text(
                text = "Şifre Başarıyla Sıfırlandı!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            // Success message
            Text(
                text = "Artık yeni şifrenizle giriş yapabilirsiniz.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Back to login button
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GİRİŞ SAYFASINA DÖN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
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
fun EmailInputStepPreview() {
    StokKontrolVeYonetimSistemiTheme {
        EmailInputStep(
            email = "test@example.com",
            emailError = null,
            generalError = null,
            resetState = AuthState.Idle,
            onEmailChange = {},
            onSendResetEmail = {},
            canProceed = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CodeVerificationStepPreview() {
    StokKontrolVeYonetimSistemiTheme {
        CodeVerificationStep(
            code = "123456",
            email = "test@example.com",
            codeError = null,
            generalError = null,
            resetState = AuthState.Idle,
            onCodeChange = {},
            onVerifyCode = {},
            onResendCode = {},
            canProceed = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessStepPreview() {
    StokKontrolVeYonetimSistemiTheme {
        SuccessStep(
            onBackToLogin = {}
        )
    }
}