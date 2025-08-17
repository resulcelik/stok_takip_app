package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.GetResponse
import com.example.stokkontrolveyonetimsistemi.data.model.auth.MessageResponse
import com.example.stokkontrolveyonetimsistemi.data.model.location.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Location management API service interface
 * Hiyerarşik lokasyon yönetimi endpoint'leri
 *
 * Dosya Konumu: /data/network/api/LocationApiService.kt
 * Backend URL pattern: http://10.0.2.2:8080/api/user/settings/
 * Authentication required: Bearer JWT token
 */
interface LocationApiService {

    // ==========================================
    // HIERARCHICAL LOCATION ENDPOINTS
    // ==========================================

    /**
     * Get all regions (Bölgeler)
     * GET /api/user/settings/bolge/all
     *
     * @return List of all available regions
     */
    @GET(ApiConstants.SETTINGS_BOLGE_ALL)
    suspend fun getAllBolge(): Response<GetResponse<List<BolgeDto>>>

    /**
     * Get provinces by region (İller by Bölge)
     * GET /api/user/settings/il/by-parent/{bolgeId}
     *
     * @param bolgeId Selected region ID
     * @return List of provinces in the selected region
     */
    @GET(ApiConstants.SETTINGS_IL_BY_PARENT + "{bolgeId}")
    suspend fun getIllerByBolge(
        @Path("bolgeId") bolgeId: Long
    ): Response<GetResponse<List<IlDto>>>

    /**
     * Get districts by province (İlçeler by İl)
     * GET /api/user/settings/ilce/by-parent/{ilId}
     *
     * @param ilId Selected province ID
     * @return List of districts in the selected province
     */
    @GET(ApiConstants.SETTINGS_ILCE_BY_PARENT + "{ilId}")
    suspend fun getIlcelerByIl(
        @Path("ilId") ilId: Long
    ): Response<GetResponse<List<IlceDto>>>

    /**
     * Get warehouses by district (Depolar by İlçe)
     * GET /api/user/settings/depo/by-parent/{ilceId}
     *
     * @param ilceId Selected district ID
     * @return List of warehouses in the selected district
     */
    @GET(ApiConstants.SETTINGS_DEPO_BY_PARENT + "{ilceId}")
    suspend fun getDepolarByIlce(
        @Path("ilceId") ilceId: Long
    ): Response<GetResponse<List<DepoDto>>>

    // ==========================================
    // UTILITY ENDPOINTS (for metadata)
    // ==========================================

    /**
     * Get stock units for product creation
     * GET /api/user/settings/stokbirimi/all
     *
     * @return List of all available stock units
     */
    @GET(ApiConstants.SETTINGS_STOK_BIRIMI_ALL)
    suspend fun getAllStokBirimi(): Response<GetResponse<List<StokBirimiDto>>>

    // NOTE: Marka endpoint'i yok - Marka dropdown olmayacak, null gönderilecek
}
