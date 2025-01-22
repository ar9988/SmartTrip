package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentEntity(
    @Json(name = "id")
    val id: Int,

    @Json(name = "content")
    val content: String,

    @Json(name = "user_id")
    val userId: String,

    @Json(name = "created_at")
    val createdAt: String
)
