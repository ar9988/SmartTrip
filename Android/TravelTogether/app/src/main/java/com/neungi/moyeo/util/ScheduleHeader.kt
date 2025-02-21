package com.neungi.moyeo.util

import java.time.LocalDateTime

data class ScheduleHeader( // Section Header data
    val dayId: Int,
    val title: String,
    val positionPath: Int,
    val startTime : LocalDateTime
)