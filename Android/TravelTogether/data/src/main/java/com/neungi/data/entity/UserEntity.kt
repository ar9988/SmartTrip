package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "email")
    val email: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "profile")
    val profile: String?,

    @Json(name = "createdAt")
    val createdAt: String?,

    @Json(name = "updatedAt")
    val updatedAt: String?
)