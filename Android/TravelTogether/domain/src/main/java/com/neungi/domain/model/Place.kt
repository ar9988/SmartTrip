package com.neungi.domain.model

data class Place (
    val placeName: String,
    val address: String,
    val lat: Double?,
    val lng: Double?,
    val imageUrl: String?=null,
    val overView:String="",
    val contentId:String = "",
    val isFollowed:Boolean = false
)