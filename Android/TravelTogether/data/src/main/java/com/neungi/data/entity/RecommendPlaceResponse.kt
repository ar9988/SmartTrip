package com.neungi.data.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendPlaceResponse(
    @Json(name = "title")
    val title: String,

    @Json(name = "imageUrl")
    val imageUrl: String,

    @Json(name = "address")
    val address: String,

    @Json(name = "overView")
    val overView: String?,

    @Json(name="contentId")
    val contentId: String,

    @Json(name="isFollowed")
    val isFollowed: Boolean

    )