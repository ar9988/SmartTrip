package com.neungi.domain.model

data class Schedule(
    val id: String,
    val placeName: String,
    val tripId: String,
    val order: Int,
    val day: Int,
    val lat: Float,
    val lng: Float,
    val type: Int
)