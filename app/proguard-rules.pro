# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Google Error Prone Annotations (Tink bağımlılığı)
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Google Crypto Tink
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# Security Crypto için
-keep class androidx.security.crypto.** { *; }

# JWT (JJWT) için
-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Retrofit & OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Data sınıflarınız için (com.example.stokkontrolveyonetimsistemi paketinizdeki modeller)
-keep class com.example.stokkontrolveyonetimsistemi.data.model.** { *; }
-keep class com.example.stokkontrolveyonetimsistemi.domain.model.** { *; }
-keep class com.example.stokkontrolveyonetimsistemi.presentation.**.state.** { *; }

# Koin
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Barcode Scanner (ZXing)
-keep class com.journeyapps.** { *; }
-keep class com.google.zxing.** { *; }

# Genel kurallar
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Enum'lar için
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}