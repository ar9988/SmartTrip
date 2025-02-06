package com.neungi.domain.model

data class Route(
    val dayId: String,
    val publicTransport: PublicTransport,
    val personalVehicle: PersonalVehicle
)

data class PublicTransport(
    val type: Int,
    val duration: Int
)

data class PersonalVehicle(
    val type: Int,
    val duration: Int
)
