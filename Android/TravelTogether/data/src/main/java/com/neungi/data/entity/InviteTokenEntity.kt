package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InviteTokenEntity(
    @Json(name = "token")
    val token: String
)