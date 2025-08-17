package com.example.stokkontrolveyonetimsistemi.data.model.session

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserProfileDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("surname")
    val surname: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("telefon")
    val telefon: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("aktif")
    val aktif: Boolean,

    @SerializedName("createdDate")
    val createdDate: String?,

    @SerializedName("updatedDate")
    val updatedDate: String?
)