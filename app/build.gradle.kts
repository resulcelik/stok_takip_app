plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // ❌ REMOVED: id("kotlin-kapt") - Room kullanmıyoruz
}

android {
    namespace = "com.example.stokkontrolveyonetimsistemi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.stokkontrolveyonetimsistemi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig fields for API configuration
        buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/\"")
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
            // Production API URL
            buildConfigField("String", "BASE_URL", "\"https://your-production-api.com/\"")
            buildConfigField("boolean", "DEBUG_MODE", "false")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
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
        buildConfig = true  // BuildConfig support
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
    // COMPOSE & UI CORE (MEVCUT - KORUNDU)
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

    // ==========================================
    // BARCODE SCANNING (MEVCUT - KORUNDU)
    // ==========================================
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)

    // Advanced Camera support
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ==========================================
    // NETWORK & API (YENİ - ENTERPRISE)
    // ==========================================
    // Retrofit for REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // ==========================================
    // JWT AUTHENTICATION (YENİ - SECURITY)
    // ==========================================
    // JWT for Android (conflict-free versions)
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
    // DEPENDENCY INJECTION (YENİ - ARCHITECTURE)
    // ==========================================
    implementation("io.insert-koin:koin-android:3.5.3") {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    implementation("io.insert-koin:koin-androidx-compose:3.5.3") {
        exclude(group = "org.jetbrains", module = "annotations")
    }

    // ==========================================
    // ❌ REMOVED: LOCAL DATABASE - Room kullanmıyoruz şu anda
    // ==========================================
    // Room dependency'lerini kaldırdık - gelecekte eklenecek
    // implementation("androidx.room:room-runtime:2.6.1")
    // implementation("androidx.room:room-ktx:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1")

    // ==========================================
    // ADDITIONAL UTILITIES (YENİ - ENTERPRISE)
    // ==========================================
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Image loading (for product images)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // SwipeRefresh support
    implementation("androidx.compose.material:material:1.5.4")

    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Date/Time handling
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // ==========================================
    // HARDWARE INTEGRATION (YENİ - ENTERPRISE)
    // ==========================================
    // Vibration feedback
    implementation("androidx.core:core:1.12.0")

    // External scanner support libraries
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    // ==========================================
    // TESTING (CONFLICT-FREE)
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