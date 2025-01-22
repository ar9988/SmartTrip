package com.neungi.domain.model

data class Route(
    val id: String,
    val tripId: String,
    val day: Int,
    val order: Int,
    val driveDuration: Int,
    val transDuration: Int
)