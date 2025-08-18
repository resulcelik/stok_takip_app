package com.example.stokkontrolveyonetimsistemi.data.repository

import android.util.Log
import com.example.stokkontrolveyonetimsistemi.data.model.product.*
import com.example.stokkontrolveyonetimsistemi.data.network.api.UrunEtiketApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * ÜRÜN Etiket Repository - DÜZELTİLMİŞ VERSİYON
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
            val response = apiService.generateUrunEtiketleri(
                adet = adet,
                boyut = "STANDART",
                herBatchteAdet = adet
            )

            Log.d(TAG, "API Response Code: ${response.code()}")

            // Response işleme
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                if (body.status == 200 && body.data != null) {
                    val data = body.data
                    val etiketData = data.etiketData

                    // ✅ DÜZELTİLDİ: urunSeriNo'yu doğru yerden al
                    var urunSeriNo = data.urunSeriNo

                    // Eğer data.urunSeriNo boşsa, etiketler'den al
                    if (urunSeriNo.isNullOrBlank() && etiketData?.etiketler?.isNotEmpty() == true) {
                        urunSeriNo = etiketData.etiketler.first().urunSeriNo
                        Log.d(TAG, "urunSeriNo etiketler'den alındı: $urunSeriNo")
                    }

                    Log.d(TAG, "=================================")
                    Log.d(TAG, "ÜRÜN ETİKET VERİLERİ:")
                    Log.d(TAG, "=================================")
                    Log.d(TAG, "Backend urunSeriNo (data): ${data.urunSeriNo}")
                    Log.d(TAG, "Kullanılacak numara: $urunSeriNo")
                    Log.d(TAG, "Numara uzunluğu: ${urunSeriNo?.length ?: 0}")
                    Log.d(TAG, "Toplam adet: ${data.adet}")
                    Log.d(TAG, "Etiket sayısı: ${etiketData?.etiketler?.size ?: 0}")

                    // Etiketlerdeki numaraları da logla
                    etiketData?.etiketler?.forEachIndexed { index, etiket ->
                        Log.d(TAG, "Etiket[$index]: ${etiket.urunSeriNo}")
                    }
                    Log.d(TAG, "=================================")

                    if (!urunSeriNo.isNullOrBlank() && etiketData != null) {
                        // Basitleştirilmiş response oluştur
                        val simplifiedResponse = UrunBarkodResponse(
                            urunSeriNo = urunSeriNo,
                            toplamAdet = data.adet,
                            etiketler = etiketData.etiketler ?: emptyList()
                        )

                        Log.d(TAG, "✅ ÜRÜN etiketi üretimi başarılı")
                        Log.d(TAG, "   - Numara: $urunSeriNo")
                        Log.d(TAG, "   - Adet: ${data.adet}")

                        emit(UrunEtiketResult.Success(simplifiedResponse))
                    } else {
                        Log.e(TAG, "❌ Backend'den eksik veri geldi")
                        Log.e(TAG, "   - data.urunSeriNo: ${data.urunSeriNo}")
                        Log.e(TAG, "   - etiketler var mı: ${etiketData?.etiketler?.isNotEmpty()}")

                        // Eğer hiç veri yoksa hata döndür
                        emit(UrunEtiketResult.Error("Üretim başarısız - Eksik veri"))
                    }
                } else {
                    Log.e(TAG, "❌ Backend hatası: Status=${body.status}, Message=${body.message}")
                    emit(UrunEtiketResult.Error(body.message ?: "Üretim başarısız"))
                }
            } else {
                Log.e(TAG, "❌ HTTP hatası: ${response.code()}")
                Log.e(TAG, "   - Response body: ${response.errorBody()?.string()}")
                emit(UrunEtiketResult.Error("Sunucu hatası: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ÜRÜN etiketi üretim hatası", e)
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