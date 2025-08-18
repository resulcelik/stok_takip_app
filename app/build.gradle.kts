plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.stokkontrolveyonetimsistemi"
    compileSdk = 35

    defaultConfig {
        multiDexEnabled = false
        applicationId = "com.example.stokkontrolveyonetimsistemi"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig fields for API configuration
        buildConfigField("String", "BASE_URL", "\"https://10.10.10.65:8080/\"")
        buildConfigField("String", "API_VERSION", "\"api/\"")
        buildConfigField("boolean", "DEBUG_MODE", "true")

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production API URL'inizi güncelleyin
            buildConfigField("String", "BASE_URL", "\"https://78.187.40.116:8443/\"") // Production URL'nizi buraya yazın
            buildConfigField("boolean", "DEBUG_MODE", "false")

            // Signing config ekleyin (eğer yoksa)
            signingConfig = signingConfigs.getByName("debug") // Geçici olarak debug key kullanabilirsiniz
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    // ==========================================
    // COMPOSE & UI CORE
    // ==========================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Compose Runtime - BOM ile yönetilir
    implementation(libs.runtime.livedata)

    // ==========================================
    // BARCODE SCANNING
    // ==========================================
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)

    // ==========================================
    // CAMERA
    // ==========================================
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ==========================================
    // NETWORK & API
    // ==========================================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ==========================================
    // JWT AUTHENTICATION
    // ==========================================
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5") {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    implementation("io.jsonwebtoken:jjwt-orgjson:0.11.5") {
        exclude(group = "org.jetbrains", module = "annotations")
    }

    // Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ==========================================
    // DEPENDENCY INJECTION
    // ==========================================
    implementation("io.insert-koin:koin-android:3.5.3") {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    implementation("io.insert-koin:koin-androidx-compose:3.5.3") {
        exclude(group = "org.jetbrains", module = "annotations")
    }

    // ==========================================
    // UTILITIES
    // ==========================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // SwipeRefresh support - Compose BOM ile otomatik yönetilir
    implementation("androidx.compose.material:material")

    // ==========================================
    // HARDWARE INTEGRATION
    // ==========================================
    implementation("androidx.core:core:1.12.0")

    // External libraries (printer JAR/AAR files)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    // ==========================================
    // TESTING
    // ==========================================
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8") {
        exclude(group = "org.jetbrains", module = "annotations")
    }

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}