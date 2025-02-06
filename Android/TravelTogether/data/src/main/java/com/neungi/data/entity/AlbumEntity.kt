package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "tripId")
    val tripId: Int,

    @Json(name = "tripTitle")
    val tripTitle: String,

    @Json(name = "repImage")
    val repImage: String,

    @Json(name = "startDate")
    val startDate: String,

    @Json(name = "endDate")
    val endDate: String
)
