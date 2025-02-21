package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoEntity(
    @Json(name = "photoId")
    val id: Int,

    @Json(name = "filePath")
    val url: String,

    @Json(name = "latitude")
    val latitude: Double,

    @Json(name = "longitude")
    val longitude: Double,

    @Json(name = "takenAt")
    val takenAt: String?,

    @Json(name = "albumId")
    val albumId: Int,

    @Json(name = "userId")
    val userId: Int,

    @Json(name = "place")
    val place: String
)
