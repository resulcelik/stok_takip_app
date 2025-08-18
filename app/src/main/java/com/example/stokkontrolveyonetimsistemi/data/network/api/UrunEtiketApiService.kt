// data/network/api/UrunEtiketApiService.kt

package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.product.UrunGetResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ÜRÜN Etiket API Service
 */
interface UrunEtiketApiService {

    /**
     * ÜRÜN etiketi üret
     * POST /api/user/main/urun/generate-labels?adet=100
     */
    @POST(ApiConstants.URUN_GENERATE_LABELS)
    suspend fun generateUrunEtiketleri(
        @Query("adet") adet: Int,
        @Query("boyut") boyut: String = "STANDART",
        @Query("herBatchteAdet") herBatchteAdet: Int
    ): Response<UrunGetResponse>

}