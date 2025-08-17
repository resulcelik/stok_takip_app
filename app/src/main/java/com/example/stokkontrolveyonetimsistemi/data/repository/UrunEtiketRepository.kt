package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.product.*
import com.example.stokkontrolveyonetimsistemi.data.network.api.UrunEtiketApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * ÜRÜN Etiket Repository - GERÇEK BACKEND YAPISINA UYGUN
 */
class UrunEtiketRepository(
    private val apiService: UrunEtiketApiService
) {

    companion object {
        private const val TAG = "UrunEtiketRepository"
    }

    /**
     * ÜRÜN etiketleri üret
     */
    fun generateUrunEtiketleri(adet: Int): Flow<UrunEtiketResult<UrunBarkodResponse>> = flow {
        try {
            emit(UrunEtiketResult.Loading)

            // Validasyon
            if (adet !in 1..100) {
                emit(UrunEtiketResult.Error("Etiket adedi 1-100 arasında olmalıdır"))
                return@flow
            }

            Log.d(TAG, "ÜRÜN etiketi üretimi başlatılıyor: $adet adet")

            // API çağrısı
            val response = apiService.generateUrunEtiketleri(adet)

            // Response işleme
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                if (body.status == 200 && body.data != null) {
                    val data = body.data
                    val etiketData = data.etiketData
                    val urunSeriNo = data.urunSeriNo

                    if (!urunSeriNo.isNullOrBlank() && etiketData != null) {
                        Log.d(TAG, "Backend'den gelen numara: $urunSeriNo")
                        Log.d(TAG, "Toplam adet: ${data.adet}")

                        // Basitleştirilmiş response oluştur
                        val simplifiedResponse = UrunBarkodResponse(
                            urunSeriNo = urunSeriNo,
                            toplamAdet = data.adet,
                            etiketler = etiketData.etiketler ?: emptyList()
                        )

                        emit(UrunEtiketResult.Success(simplifiedResponse))
                    } else {
                        Log.e(TAG, "Backend'den eksik veri geldi")
                        emit(UrunEtiketResult.Error("Üretim başarısız - Eksik veri"))
                    }
                } else {
                    Log.e(TAG, "Backend hatası: ${body.message}")
                    emit(UrunEtiketResult.Error(body.message ?: "Üretim başarısız"))
                }
            } else {
                Log.e(TAG, "HTTP hatası: ${response.code()}")
                emit(UrunEtiketResult.Error("Sunucu hatası: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "ÜRÜN etiketi üretim hatası", e)
            emit(UrunEtiketResult.Error(getErrorMessage(e)))
        }
    }

    /**
     * Error message formatter
     */
    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is java.net.UnknownHostException -> "İnternet bağlantısı yok"
            is java.net.SocketTimeoutException -> "Bağlantı zaman aşımı"
            is java.net.ConnectException -> "Sunucuya bağlanılamıyor"
            is java.io.IOException -> "Ağ hatası"
            else -> "Beklenmeyen hata: ${exception.localizedMessage}"
        }
    }
}