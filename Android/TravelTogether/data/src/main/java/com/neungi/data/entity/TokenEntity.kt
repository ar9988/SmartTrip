package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "email")
    val email: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "profile")
    val profile: String?,

    @Json(name = "profile_image")
    val profileImage: String?,

    @Json(name = "token")
    val token: String
)