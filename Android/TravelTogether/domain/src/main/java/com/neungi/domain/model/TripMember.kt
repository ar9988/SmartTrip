package com.neungi.domain.model

data class TripMember(
    val id: String,
    val tripId: String,
    val userId: String,
    val isOwner: Boolean
)