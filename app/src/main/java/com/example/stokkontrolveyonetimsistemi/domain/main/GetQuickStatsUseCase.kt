package com.example.stokkontrolveyonetimsistemi.domain.main

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.local.storage.TokenStorage
import com.example.stokkontrolveyonetimsistemi.data.model.dashboard.QuickStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*


class GetQuickStatsUseCase(
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "GetQuickStatsUseCase"
    }

    suspend fun execute(forceRefresh: Boolean = false): Flow<Result<QuickStats>> = flow {
        try {
            Log.d(TAG, "Loading quick statistics (forceRefresh: $forceRefresh)")

            // Check authentication first
            if (!tokenStorage.isTokenValid()) {
                Log.w(TAG, "Token invalid - cannot get stats")
                emit(Result.failure(Exception("Token geçersiz")))
                return@flow
            }

            // Simulate API delay for better UX
            if (forceRefresh) {
                kotlinx.coroutines.delay(500)
            }

            // TODO: Backend API call burada olacak
            // val apiResponse = statsApiService.getTodayStats()

            // Şimdilik boş stats döndür (gerçek veriler backend'den gelecek)
            val quickStats = QuickStats(
                todayScanned = 0,
                todayProducts = 0,
                todayShelf = 0,
                userScanCount = 0,
                userLastScan = null,
                lastUpdated = Date(),
                dataAge = 0L
            )

            Log.d(TAG, "Quick statistics loaded (empty - waiting for backend API)")
            emit(Result.success(quickStats))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get quick stats", e)
            emit(Result.failure(e))
        }
    }
}