package com.neungi.data.entity

import com.neungi.domain.model.ScheduleData
import java.time.LocalDateTime

data class ScheduleReceive(
    val tripId: Int,
    val title: String,
    val members: List<Member>,
    val day: List<Day>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Member(
    val userId: String,
    val name: String,
    val owner: Boolean,
    val profileImage:String
)

data class Day(
    val startTime: LocalDateTime,
    val schedules: List<ScheduleData>
)