package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "url")
    val url: String,

    @Json(name = "latitude")
    val latitude: Float,

    @Json(name = "longitude")
    val longitude: Float,

    @Json(name = "created_at")
    val createdAt: String
)
