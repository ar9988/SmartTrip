package com.neungi.domain.model

data class AddRequest (
    val action: String,
    val tripId:Int,
    val dayId:Int,
    val placeName: String,
    val placeType: Int,
)