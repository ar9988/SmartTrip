package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class FestivalEntity(
    @Json(name = "title")
    val title: String,

    @Json(name = "address")
    val address: String,

    @Json(name = "imageurl")
    val imageUrl: String,

    @Json(name = "eventStartDate")
    val eventStartDate: String,

    @Json(name = "eventEndDate")
    val eventEndDate: String,

    @Json(name = "contentid")
    val contentId: String

)
