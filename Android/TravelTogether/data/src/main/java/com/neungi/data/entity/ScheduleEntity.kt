package com.neungi.data.entity

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize


@Parcelize
@JsonClass(generateAdapter = true)
data class ScheduleEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "place_name")
    val placeName: String,

    @Json(name = "trip_id")
    var tripId: Int,

    @Json(name = "positionPath")
    val positionPath: Int,

    @Json(name = "day")
    val day: Int,

    @Json(name = "lat")
    val lat: Double,

    @Json(name = "lng")
    val lng: Double,

    @Json(name = "type")
    val type: Int,

    @Json(name = "duration")
    var duration: Int
) : Parcelable
