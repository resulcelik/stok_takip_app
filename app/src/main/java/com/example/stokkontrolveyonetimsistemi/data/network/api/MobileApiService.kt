package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.raf.RafCreateRequest
import com.example.stokkontrolveyonetimsistemi.data.model.urun.UrunRequest
import com.example.stokkontrolveyonetimsistemi.response.CreateResponse
import com.example.stokkontrolveyonetimsistemi.response.MessageResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MobileApiService {

    @POST(ApiConstants.RAF_CREATE_MOBILE)
    suspend fun createMobileRaf(
        @Body request: RafCreateRequest
    ): Response<MessageResponse>


    @POST("api/user/main/urun/create")
    suspend fun createUrun(
        @Body request: UrunRequest
    ): Response<CreateResponse>

    @Multipart
    @POST("api/user/main/mobile/file/urun/{id}/upload-photo")
    suspend fun uploadUrunPhoto(
        @Path("id") id: Long,
        @Part photo: MultipartBody.Part
    ): Response<MessageResponse>


    @Multipart
    @POST("api/user/main/mobile/file/urun/{urunSeriNo}/upload-multiple-photos")
    suspend fun uploadMultipleUrunPhotos(
        @Path("urunSeriNo") urunSeriNo: String,
        @Part photos: List<MultipartBody.Part>
    ): Response<MessageResponse>
}

data class MessageResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String
)