package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.shelf.*
import com.example.stokkontrolveyonetimsistemi.data.network.api.RafEtiketApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * RAF Etiket Repository - SadeleÅŸtirilmiÅŸ
 * Tek iÅŸlev: Backend'den RAF etiketi Ã¼retmek
 */
class RafEtiketRepository(
    private val apiService: RafEtiketApiService,
    private val gson: Gson = Gson()
) {

    companion object {
        private const val TAG = "RafEtiketRepository"
    }

    /**
     * RAF etiketi Ã¼ret
     * @param adet Etiket adedi (1-1000)
     * @return Flow with result
     */
    fun generateRafEtiketleri(adet: Int): Flow<RafEtiketResult<List<String>>> = flow {
        try {
            emit(RafEtiketResult.Loading)

            // Validasyon
            if (adet !in 1..1000) {
                emit(RafEtiketResult.Error("Etiket adedi 1-1000 arasÄ±nda olmalÄ±dÄ±r"))
                return@flow
            }

            Log.d(TAG, "RAF etiket Ã¼retimi baÅŸlatÄ±ldÄ±: $adet adet")

            // API Ã§aÄŸrÄ±sÄ± - Sabit STANDART boyut
            val response = apiService.generateRafEtiketleri(
                adet = adet,
                boyut = "STANDART"
            )

            if (response.isSuccessful) {
                val getResponse = response.body()

                if (getResponse != null && getResponse.isSuccess()) {
                    // data'yÄ± BarkodResponse'a dÃ¶nÃ¼ÅŸtÃ¼r
                    val barkodResponse = gson.fromJson(
                        gson.toJson(getResponse.data),
                        BarkodResponse::class.java
                    )

                    val rafNumbers = barkodResponse.getRafNumbers()

                    Log.d(TAG, "RAF etiketleri Ã¼retildi: ${rafNumbers.size} adet")
                    Log.d(TAG, "Ä°lk numara: ${rafNumbers.firstOrNull()}")
                    Log.d(TAG, "Son numara: ${rafNumbers.lastOrNull()}")

                    emit(RafEtiketResult.Success(rafNumbers))
                } else {
                    val errorMsg = getResponse?.message ?: "Bilinmeyen hata"
                    Log.e(TAG, "API hatasÄ±: $errorMsg")
                    emit(RafEtiketResult.Error(errorMsg))
                }
            } else {
                val errorMsg = "Sunucu hatasÄ±: ${response.code()}"
                Log.e(TAG, errorMsg)
                emit(RafEtiketResult.Error(errorMsg))
            }

        } catch (e: Exception) {
            Log.e(TAG, "RAF etiket Ã¼retim hatasÄ±", e)
            emit(RafEtiketResult.Error("BaÄŸlantÄ± hatasÄ±: ${e.localizedMessage}"))
        }
    }
}