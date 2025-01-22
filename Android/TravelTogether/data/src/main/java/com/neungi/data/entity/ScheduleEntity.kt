package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "place_name")
    val placeName: String,

    @Json(name = "trip_id")
    val tripId: Int,

    @Json(name = "order")
    val order: Int,

    @Json(name = "day")
    val day: Int,

    @Json(name = "lat")
    val lat: Double,

    @Json(name = "lng")
    val lng: Double,

    @Json(name = "type")
    val type: Int
)
