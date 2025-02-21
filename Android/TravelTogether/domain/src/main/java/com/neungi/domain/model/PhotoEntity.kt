package com.neungi.domain.model

import okhttp3.MultipartBody

data class PhotoEntity(
    val place: String,
    val body: MultipartBody.Part?,
    val latitude: Double,
    val longitude: Double,
    val takenAt: String
)