package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class OverViewEntity (
    @Json(name = "overview")
    val overview: String
)