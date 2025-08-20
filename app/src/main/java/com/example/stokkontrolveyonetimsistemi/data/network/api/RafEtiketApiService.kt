package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.shelf.GetResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface RafEtiketApiService {

    @POST(ApiConstants.RAF_GENERATE_LABELS)
    suspend fun generateRafEtiketleri(
        @Query("adet") adet: Int,
        @Query("boyut") boyut: String = "STANDART"  // Sabit
    ): Response<GetResponse>
}