package com.example.stokkontrolveyonetimsistemi.data.model.printer

import androidx.annotation.Keep

sealed class PrintResult {
    data class Success(val message: String, val printedCount: Int) : PrintResult()
    data class Error(val message: String) : PrintResult()
}

@Keep
sealed class PrinterStatus {
    data class Error(val message: String) : PrinterStatus()
}