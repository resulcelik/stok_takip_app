package com.example.stokkontrolveyonetimsistemi.response

data class CreateResponse(
    val status: Int,
    val message: String,
    val id: Long  // Backend'deki 'long id' ile eşleşmeli
){fun isSuccess(): Boolean = status == 200}