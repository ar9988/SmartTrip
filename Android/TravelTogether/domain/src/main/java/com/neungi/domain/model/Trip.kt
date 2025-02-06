package com.neungi.domain.model

import java.time.ZonedDateTime

data class Trip(
    val id: Int,
    val creatorId: String,
    val title: String,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val tripParticipants: Int
)