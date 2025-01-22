package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "trip_id")
    val tripId: Int,

    @Json(name = "trip_title")
    val tripTitle: String,

    @Json(name = "rep_image")
    val repImage: String,

    @Json(name = "start_date")
    val startDate: String,

    @Json(name = "end_date")
    val endDate: String
)
