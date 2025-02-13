package com.neungi.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CreateTripRequest(
    val title: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val userId: Int,
)

