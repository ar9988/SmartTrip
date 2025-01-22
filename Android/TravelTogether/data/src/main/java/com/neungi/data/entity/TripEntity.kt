package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.ZonedDateTime
import java.util.Date

@JsonClass(generateAdapter = true)
data class TripEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name="title")
    val title: String,

    @Json(name="start_date")
    val startDate: Date,

    @Json(name="end_date")
    val endDate:ZonedDateTime,

    @Json(name="thumbnail")
    val thumbnail:String,

    @Json(name="memberCount")
    val memberCount:Int,

    @Json(name = "status")
    val status:Boolean,

    @Json(name ="createdAt")
    val createdAt:ZonedDateTime

)
