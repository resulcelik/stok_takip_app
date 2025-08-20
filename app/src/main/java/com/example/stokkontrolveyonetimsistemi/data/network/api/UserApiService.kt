package com.example.stokkontrolveyonetimsistemi.data.network.api

import com.example.stokkontrolveyonetimsistemi.core.constants.ApiConstants
import com.example.stokkontrolveyonetimsistemi.data.model.auth.GetResponse
import com.example.stokkontrolveyonetimsistemi.data.model.auth.MessageResponse
import com.example.stokkontrolveyonetimsistemi.data.model.location.SetLocationRequest
import com.example.stokkontrolveyonetimsistemi.data.model.session.LocationValidationDto
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserProfileDto
import com.example.stokkontrolveyonetimsistemi.data.model.session.UserSessionDto
import retrofit2.Response
import retrofit2.http.*


interface UserApiService {


    @GET(ApiConstants.SESSION_CURRENT)
    suspend fun getCurrentSession(): Response<GetResponse<UserSessionDto>>

    @POST(ApiConstants.SESSION_SET_LOCATION)
    suspend fun setUserLocation(
        @Body request: SetLocationRequest
    ): Response<MessageResponse>

    @DELETE(ApiConstants.SESSION_CLEAR_LOCATION)
    suspend fun clearUserLocation(): Response<MessageResponse>


}