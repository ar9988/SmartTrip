package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InviteAcceptEntity(
    @Json(name = "message")
    val message: String,

    @Json(name = "tripId")
    val tripId: Int
)