package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.shelf.GetResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * RAF Etiket API Service - Sadeleştirilmiş
 * Tek işlev: Adet girip standart boyutta RAF etiketi üretmek
 */
interface RafEtiketApiService {

    /**
     * RAF etiketi üret
     * POST /api/user/main/raf/generate-labels?adet=100&boyut=STANDART
     *
     * @param adet Etiket adedi (1-1000)
     * @return GetResponse with BarkodResponse data
     */
    @POST(ApiConstants.RAF_GENERATE_LABELS)
    suspend fun generateRafEtiketleri(
        @Query("adet") adet: Int,
        @Query("boyut") boyut: String = "STANDART"  // Sabit
    ): Response<GetResponse>
}