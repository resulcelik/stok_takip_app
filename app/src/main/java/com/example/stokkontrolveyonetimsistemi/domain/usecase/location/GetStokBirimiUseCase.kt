package com.example.stokkontrolveyonetimsistemi.domain.usecase.location

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import com.example.stokkontrolveyonetimsistemi.data.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Get Stok Birimi Use Case
 * Ürün ekleme formu için stok birimi listesi business logic
 *
 * Dosya Konumu: /domain/usecase/location/GetStokBirimiUseCase.kt
 * Purpose: Product creation form dropdown data
 */
class GetStokBirimiUseCase(
    private val locationRepository: LocationRepository
) {

    companion object {
        private const val TAG = "GetStokBirimiUseCase"
    }

    // ==========================================
    // MAIN OPERATIONS
    // ==========================================

    /**
     * Get all stock units
     * Ürün ekleme formu için stok birimi dropdown data
     *
     * @return List of available stock units
     */
    suspend fun execute(): Flow<LocationResult<List<StokBirimiDto>>> {
        Log.d(TAG, "Loading stock units for product form")
        return locationRepository.getAllStokBirimi()
    }

    /**
     * Get stock units with caching
     * Cache ile optimize edilmiş stok birimi loading
     *
     * @param forceRefresh Cache'i bypass et ve fresh data al
     * @return List of stock units
     */
    suspend fun executeWithCache(forceRefresh: Boolean = false): Flow<LocationResult<List<StokBirimiDto>>> {
        Log.d(TAG, "Loading stock units (forceRefresh: $forceRefresh)")

        // TODO: Cache implementation eklenebilir (gelecekte)
        // Şimdilik direkt repository'den al
        return locationRepository.getAllStokBirimi()
    }

    // ==========================================
    // BUSINESS LOGIC OPERATIONS
    // ==========================================

    /**
     * Get default stock unit
     * Form için default seçim (örnek: "Adet" en çok kullanılan ise)
     */
    suspend fun getDefaultStokBirimi(): Flow<LocationResult<StokBirimiDto?>> {
        return kotlinx.coroutines.flow.flow {
            try {
                Log.d(TAG, "Finding default stock unit")

                locationRepository.getAllStokBirimi().collect { result ->
                    when (result) {
                        is LocationResult.Success -> {
                            // Business logic: Default stock unit seçimi
                            val defaultUnit = findDefaultStockUnit(result.data)
                            emit(LocationResult.Success(defaultUnit))
                        }
                        is LocationResult.Error -> {
                            emit(LocationResult.Error(result.message))
                        }
                        is LocationResult.TokenExpired -> {
                            emit(LocationResult.TokenExpired)
                        }
                        is LocationResult.Loading -> {
                            emit(LocationResult.Loading)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error getting default stock unit", e)
                emit(LocationResult.Error("Default stok birimi bulunamadı"))
            }
        }
    }

    /**
     * Validate stock unit selection
     * Stok birimi seçiminin geçerliliğini kontrol et
     */
    fun validateStokBirimiSelection(
        stockUnitId: Long?,
        availableUnits: List<StokBirimiDto>
    ): LocationResult<StokBirimiDto> {

        return when {
            stockUnitId == null || stockUnitId <= 0 -> {
                LocationResult.Error("Stok birimi seçimi gerekli")
            }

            availableUnits.isEmpty() -> {
                LocationResult.Error("Stok birimi listesi boş")
            }

            else -> {
                val selectedUnit = availableUnits.find { it.id == stockUnitId }
                if (selectedUnit != null) {
                    LocationResult.Success(selectedUnit)
                } else {
                    LocationResult.Error("Seçilen stok birimi geçersiz")
                }
            }
        }
    }

    // ==========================================
    // HELPER OPERATIONS
    // ==========================================

    /**
     * Find default stock unit from list
     * Business logic: En yaygın kullanılan stok birimini bul
     */
    private fun findDefaultStockUnit(stockUnits: List<StokBirimiDto>): StokBirimiDto? {
        if (stockUnits.isEmpty()) return null

        // Business logic: Öncelik sırası
        val priorityNames = listOf(
            "ADET", "Adet", "adet",
            "KG", "kg", "Kg", "Kilogram",
            "LT", "lt", "Lt", "Litre",
            "MT", "mt", "Mt", "Metre"
        )

        // 1. Öncelik listesindeki ilk eşleşmeyi bul
        for (priorityName in priorityNames) {
            val found = stockUnits.find {
                it.stokBirimiAdi.equals(priorityName, ignoreCase = true) ||
                        it.kisaAd?.equals(priorityName, ignoreCase = true) == true
            }
            if (found != null) {
                Log.d(TAG, "Default stock unit found: ${found.getDisplayName()}")
                return found
            }
        }

        // 2. Eğer hiçbiri bulunamazsa ilk item'ı döndür
        val defaultUnit = stockUnits.first()
        Log.d(TAG, "Using first stock unit as default: ${defaultUnit.getDisplayName()}")
        return defaultUnit
    }

    /**
     * Get stock units formatted for UI dropdown
     * UI için optimize edilmiş stok birimi listesi
     */
    suspend fun getFormattedStokBirimi(): Flow<LocationResult<List<StokBirimiDisplayItem>>> {
        return kotlinx.coroutines.flow.flow {
            try {
                Log.d(TAG, "Getting formatted stock units for UI")

                locationRepository.getAllStokBirimi().collect { result ->
                    when (result) {
                        is LocationResult.Success -> {
                            val formattedItems = result.data.map { stockUnit ->
                                StokBirimiDisplayItem(
                                    id = stockUnit.id,
                                    displayName = stockUnit.getDisplayName(),
                                    shortName = stockUnit.getShortName(),
                                    originalDto = stockUnit
                                )
                            }
                            emit(LocationResult.Success(formattedItems))
                        }
                        is LocationResult.Error -> {
                            emit(LocationResult.Error(result.message))
                        }
                        is LocationResult.TokenExpired -> {
                            emit(LocationResult.TokenExpired)
                        }
                        is LocationResult.Loading -> {
                            emit(LocationResult.Loading)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error formatting stock units", e)
                emit(LocationResult.Error("Stok birimi formatlanamadı"))
            }
        }
    }
}

// ==========================================
// UI HELPER DATA CLASSES
// ==========================================

/**
 * Stock unit display item for UI dropdown
 * UI dropdown'ı için optimize edilmiş stok birimi item'ı
 */
data class StokBirimiDisplayItem(
    val id: Long,
    val displayName: String,
    val shortName: String,
    val originalDto: StokBirimiDto
) {
    /**
     * UI'da gösterilecek text
     */
    fun getUIText(): String = displayName

    /**
     * Form validation için ID
     */
    fun getValueForForm(): Long = id
}