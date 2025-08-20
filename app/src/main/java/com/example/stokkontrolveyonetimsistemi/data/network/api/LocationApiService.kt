package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.GetResponse
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import retrofit2.Response
import retrofit2.http.*


interface LocationApiService {

    @GET(ApiConstants.SETTINGS_BOLGE_ALL)
    suspend fun getAllBolge(): Response<GetResponse<List<BolgeDto>>>


    @GET(ApiConstants.SETTINGS_IL_BY_PARENT + "{bolgeId}")
    suspend fun getIllerByBolge(
        @Path("bolgeId") bolgeId: Long
    ): Response<GetResponse<List<IlDto>>>


    @GET(ApiConstants.SETTINGS_ILCE_BY_PARENT + "{ilId}")
    suspend fun getIlcelerByIl(
        @Path("ilId") ilId: Long
    ): Response<GetResponse<List<IlceDto>>>


    @GET(ApiConstants.SETTINGS_DEPO_BY_PARENT + "{ilceId}")
    suspend fun getDepolarByIlce(
        @Path("ilceId") ilceId: Long
    ): Response<GetResponse<List<DepoDto>>>


    @GET(ApiConstants.SETTINGS_STOK_BIRIMI_ALL)
    suspend fun getAllStokBirimi(): Response<GetResponse<List<StokBirimiDto>>>
}
