package com.example.stokkontrolveyonetimsistemi.core.constants

import android.util.Log
import java.util.regex.Pattern

/**
 * Barcode validation, formatting and utility functions
 * Barkod doğrulama, formatlama ve yardımcı fonksiyonlar
 *
 * Backend format:
 * - Product codes: U00000000001 (11 digits after U)
 * - Shelf codes: R00000000001 (11 digits after R)
 */
object BarcodeUtils {

    private const val TAG = "BarcodeUtils"

    // ==========================================
    // VALIDATION PATTERNS
    // ==========================================

    // Product codes: U + 11 digits
    private val PRODUCT_CODE_PATTERN = Pattern.compile(AppConstants.PRODUCT_CODE_PATTERN)

    // Shelf codes: R + 11 digits
    private val SHELF_CODE_PATTERN = Pattern.compile(AppConstants.SHELF_CODE_PATTERN)

    // Generic barcode patterns (for external barcodes)
    private val EAN13_PATTERN = Pattern.compile("^\\d{13}$")
    private val EAN8_PATTERN = Pattern.compile("^\\d{8}$")
    private val CODE128_PATTERN = Pattern.compile("^[\\x00-\\x7F]+$")
    private val QR_CODE_PATTERN = Pattern.compile("^.{1,4296}$") // QR can be up to 4296 chars

    // ==========================================
    // VALIDATION FUNCTIONS
    // ==========================================

    /**
     * Check if barcode is valid product code (U00000000001 format)
     */
    fun isValidProductCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return PRODUCT_CODE_PATTERN.matcher(trimmed).matches()
    }

    /**
     * Check if barcode is valid shelf code (R00000000001 format)
     */
    fun isValidShelfCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return SHELF_CODE_PATTERN.matcher(trimmed).matches()
    }

    /**
     * Check if barcode is any valid system code (Product or Shelf)
     */
    fun isValidSystemCode(code: String): Boolean {
        return isValidProductCode(code) || isValidShelfCode(code)
    }

    /**
     * Validate any barcode format (including external formats)
     */
    fun validateAnyBarcode(code: String): BarcodeValidationResult {
        val trimmed = code.trim()

        return when {
            trimmed.isEmpty() ->
                BarcodeValidationResult.Invalid("Boş barkod")

            trimmed.length < 3 ->
                BarcodeValidationResult.Invalid("Çok kısa barkod (min 3 karakter)")

            isValidProductCode(trimmed) ->
                BarcodeValidationResult.Valid(trimmed.uppercase(), BarcodeType.PRODUCT)

            isValidShelfCode(trimmed) ->
                BarcodeValidationResult.Valid(trimmed.uppercase(), BarcodeType.SHELF)

            EAN13_PATTERN.matcher(trimmed).matches() ->
                BarcodeValidationResult.External(trimmed, BarcodeType.EAN13)

            EAN8_PATTERN.matcher(trimmed).matches() ->
                BarcodeValidationResult.External(trimmed, BarcodeType.EAN8)

            CODE128_PATTERN.matcher(trimmed).matches() && trimmed.length <= 80 ->
                BarcodeValidationResult.External(trimmed, BarcodeType.CODE128)

            trimmed.length <= 4296 ->
                BarcodeValidationResult.External(trimmed, BarcodeType.QR_CODE)

            else ->
                BarcodeValidationResult.Invalid("Tanımlı olmayan barkod formatı")
        }
    }

    // ==========================================
    // FORMATTING FUNCTIONS
    // ==========================================

    /**
     * Format barcode for display (user-friendly format)
     */
    fun formatForDisplay(code: String): String {
        val trimmed = code.trim().uppercase()

        return when {
            isValidProductCode(trimmed) -> {
                // U00000000001 → ÜRÜN: 000-0000-0001
                "ÜRÜN: ${trimmed.substring(1, 4)}-${trimmed.substring(4, 8)}-${trimmed.substring(8)}"
            }
            isValidShelfCode(trimmed) -> {
                // R00000000001 → RAF: 000-0000-0001
                "RAF: ${trimmed.substring(1, 4)}-${trimmed.substring(4, 8)}-${trimmed.substring(8)}"
            }
            else -> {
                // External barcode → just clean format
                trimmed
            }
        }
    }

    /**
     * Get short description of barcode type
     */
    fun getBarcodeTypeDescription(code: String): String {
        return when (getBarcodeType(code)) {
            BarcodeType.PRODUCT -> "Ürün Kodu"
            BarcodeType.SHELF -> "Raf Kodu"
            BarcodeType.EAN13 -> "EAN-13 Barkod"
            BarcodeType.EAN8 -> "EAN-8 Barkod"
            BarcodeType.CODE128 -> "Code128 Barkod"
            BarcodeType.QR_CODE -> "QR Kod"
            BarcodeType.UNKNOWN -> "Bilinmeyen Format"
        }
    }

    /**
     * Determine barcode type
     */
    fun getBarcodeType(code: String): BarcodeType {
        val trimmed = code.trim().uppercase()

        return when {
            isValidProductCode(trimmed) -> BarcodeType.PRODUCT
            isValidShelfCode(trimmed) -> BarcodeType.SHELF
            EAN13_PATTERN.matcher(trimmed).matches() -> BarcodeType.EAN13
            EAN8_PATTERN.matcher(trimmed).matches() -> BarcodeType.EAN8
            CODE128_PATTERN.matcher(trimmed).matches() && trimmed.length <= 80 -> BarcodeType.CODE128
            trimmed.length <= 4296 -> BarcodeType.QR_CODE
            else -> BarcodeType.UNKNOWN
        }
    }

    // ==========================================
    // CLEANING & SANITIZATION
    // ==========================================

    /**
     * Clean and sanitize barcode input
     */
    fun sanitizeBarcode(input: String): String {
        return input
            .trim()
            .replace("\n", "")
            .replace("\r", "")
            .replace("\t", "")
            .replace(" ", "")
            .uppercase()
    }

    /**
     * Extract numeric part from system codes
     */
    fun extractNumericPart(systemCode: String): String? {
        return when {
            isValidProductCode(systemCode) -> systemCode.substring(1) // Remove 'U'
            isValidShelfCode(systemCode) -> systemCode.substring(1)   // Remove 'R'
            else -> null
        }
    }

    // ==========================================
    // LOGGING & DEBUGGING
    // ==========================================

    /**
     * Log barcode scan for debugging
     */
    fun logScan(code: String, source: String, isValid: Boolean) {
        val type = getBarcodeType(code)
        val formatted = formatForDisplay(code)

        Log.d(TAG, "SCAN: $source | Valid: $isValid | Type: $type | Code: $formatted")
    }

    /**
     * Validate and log scan result
     */
    fun validateAndLog(code: String, source: String): BarcodeValidationResult {
        val result = validateAnyBarcode(code)

        logScan(code, source, result is BarcodeValidationResult.Valid)

        return result
    }
}

// ==========================================
// DATA CLASSES & ENUMS
// ==========================================

/**
 * Barcode validation result
 */
sealed class BarcodeValidationResult {
    data class Valid(val code: String, val type: BarcodeType) : BarcodeValidationResult()
    data class External(val code: String, val type: BarcodeType) : BarcodeValidationResult()
    data class Invalid(val reason: String) : BarcodeValidationResult()
}

/**
 * Supported barcode types
 */
enum class BarcodeType {
    PRODUCT,    // System product code (U00000000001)
    SHELF,      // System shelf code (R00000000001)
    EAN13,      // Standard EAN-13 barcode
    EAN8,       // Standard EAN-8 barcode
    CODE128,    // Code128 barcode
    QR_CODE,    // QR Code
    UNKNOWN     // Unknown or unsupported format
}

/**
 * Scan source information
 */
enum class ScanSource {
    CAMERA,           // Built-in camera scan
    EXTERNAL_SCANNER, // Hardware barcode scanner
    MANUAL_INPUT,     // Manual keyboard input
    NFC,              // NFC tag (future)
    CLIPBOARD         // Paste from clipboard
}