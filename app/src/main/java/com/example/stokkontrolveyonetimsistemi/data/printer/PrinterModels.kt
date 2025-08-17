package com.example.stokkontrolveyonetimsistemi.data.model.printer

import androidx.annotation.Keep

/**
 * Print operation result
 */
@Keep
sealed class PrintResult {
    data class Success(val message: String, val printedCount: Int) : PrintResult()
    data class Error(val message: String) : PrintResult()
}

/**
 * Printer status
 */
@Keep
sealed class PrinterStatus {
    data class Online(val message: String) : PrinterStatus()
    data class Offline(val message: String) : PrinterStatus()
    data class Error(val message: String) : PrinterStatus()
    data class Unknown(val message: String) : PrinterStatus()
}