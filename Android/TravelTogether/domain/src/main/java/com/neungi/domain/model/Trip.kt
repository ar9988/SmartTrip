package com.neungi.domain.model

data class Trip(
    val id: Int,
    val creatorId: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String,
    val tripParticipants: Int
)