package com.neungi.domain.model

import java.time.LocalTime

data class ScheduleData(
    val scheduleId: Int,
    val placeName: String,
    var positionPath: Int,
    val timeStamp: Long,
    val type: Int,
    val lat: Double,
    val lng: Double,
    var duration: Int,
    var fromTime : LocalTime?,
    var toTime : LocalTime?
)