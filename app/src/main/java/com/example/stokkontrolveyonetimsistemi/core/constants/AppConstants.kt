package com.example.stokkontrolveyonetimsistemi.core.constants

/**
 * Application-wide constants and configuration
 * App genelinde kullanılan sabitler ve konfigürasyon
 * ✅ FIXED: Constant name conflicts resolved
 */
object AppConstants {

    // ==========================================
    // APP INFORMATION
    // ==========================================
    const val APP_NAME = "Stok Kontrol Mobil"
    const val APP_VERSION = "1.0.0"
    const val COMPANY_NAME = "KeyCyte Technology"

    // ==========================================
    // SHARED PREFERENCES KEYS
    // ==========================================
    const val SHARED_PREF_NAME = "stok_kontrol_secure_prefs"  // ✅ FIXED: StokKontrolApplication kullanıyor
    const val SHARED_PREFS_NAME = "stok_kontrol_secure_prefs" // ✅ BACKUP: Çoğul versiyon da var

    const val KEY_JWT_TOKEN = "jwt_token"
    const val KEY_TOKEN_EXPIRY = "token_expiry"
    const val KEY_USER_INFO = "user_info"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_LAST_LOGIN = "last_login"
    const val KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled"
    const val KEY_SCANNER_MODE = "scanner_mode"
    const val KEY_SCAN_SOUND_ENABLED = "scan_sound_enabled"
    const val KEY_SCAN_VIBRATION_ENABLED = "scan_vibration_enabled"

    // ==========================================
    // JWT TOKEN CONFIGURATION
    // ==========================================
    const val JWT_EXPIRY_THRESHOLD_HOURS = 24L
    const val JWT_EXPIRY_THRESHOLD_MS = JWT_EXPIRY_THRESHOLD_HOURS * 60 * 60 * 1000L
    const val TOKEN_REFRESH_BEFORE_EXPIRY_MS = 2 * 60 * 60 * 1000L // 2 hours before expiry

    // ==========================================
    // BARCODE CONFIGURATION
    // ==========================================
    // Product code format: U00000000001 (11 digits)
    const val PRODUCT_CODE_PREFIX = "U"
    const val PRODUCT_CODE_LENGTH = 12
    const val PRODUCT_CODE_PATTERN = "^U\\d{11}$"

    // Shelf code format: R00000000001 (11 digits)
    const val SHELF_CODE_PREFIX = "R"
    const val SHELF_CODE_LENGTH = 12
    const val SHELF_CODE_PATTERN = "^R\\d{11}$"

    // Scanning configuration
    const val SCAN_TIMEOUT_MS = 30000L
    const val SCAN_VIBRATION_DURATION_MS = 100L
    const val SCAN_SUCCESS_SOUND_DURATION_MS = 200L

    // ==========================================
    // UI CONFIGURATION
    // ==========================================
    const val SPLASH_DISPLAY_DURATION_MS = 3000L
    const val LOADING_DIALOG_MIN_DURATION_MS = 500L
    const val TOAST_DURATION_LONG_MS = 3500L
    const val TOAST_DURATION_SHORT_MS = 2000L

    // Animation durations
    const val ANIMATION_DURATION_SHORT_MS = 200L
    const val ANIMATION_DURATION_MEDIUM_MS = 400L
    const val ANIMATION_DURATION_LONG_MS = 600L

    // ==========================================
    // VALIDATION RULES
    // ==========================================
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 50
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 50

    // Input validation patterns
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    const val PHONE_PATTERN = "^[0-9]{10,11}$"

    // ==========================================
    // DATE FORMAT PATTERNS (Turkish locale)
    // ==========================================
    const val DATE_FORMAT_DISPLAY = "dd.MM.yyyy HH:mm"
    const val DATE_FORMAT_API = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_SHORT = "dd.MM.yyyy"
    const val TIME_FORMAT = "HH:mm"

    // ==========================================
    // REQUEST INTENT EXTRAS
    // ==========================================
    const val EXTRA_PRODUCT_ID = "extra_product_id"
    const val EXTRA_SHELF_ID = "extra_shelf_id"
    const val EXTRA_SCAN_MODE = "extra_scan_mode"
    const val EXTRA_SCAN_RESULT = "extra_scan_result"
    const val EXTRA_AUTO_SCAN = "extra_auto_scan"

    // ==========================================
    // SCAN MODES
    // ==========================================
    const val SCAN_MODE_PRODUCT = "PRODUCT"
    const val SCAN_MODE_SHELF = "SHELF"
    const val SCAN_MODE_GENERAL = "GENERAL"
    const val SCAN_MODE_INVENTORY = "INVENTORY"

    // Scanner operation modes
    const val SCANNER_MODE_AUTO = "AUTO"
    const val SCANNER_MODE_MANUAL = "MANUAL"
    const val SCANNER_MODE_CONTINUOUS = "CONTINUOUS"

    // ==========================================
    // ✅ ADDED: Database configuration (currently unused)
    // ==========================================
    const val DATABASE_NAME = "stok_kontrol_database"
    const val DATABASE_VERSION = 1

    // ✅ ADDED: Missing constants
    const val MAX_SCAN_HISTORY_ITEMS = 100
}