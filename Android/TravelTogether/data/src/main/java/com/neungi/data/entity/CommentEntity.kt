package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentEntity(
    @Json(name = "commentId")
    val id: Int,

    @Json(name = "albumId")
    val albumId: Int,

    @Json(name = "photoId")
    val photoId: Int,

    @Json(name = "userId")
    val userId: Int,

    @Json(name = "userName")
    val userName: String,

    @Json(name = "profileImage")
    val profileImage: String?,

    @Json(name = "content")
    val content: String,

    @Json(name = "createdAt")
    val createdAt: String
)