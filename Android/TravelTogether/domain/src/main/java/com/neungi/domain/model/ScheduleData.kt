package com.neungi.domain.model

data class ScheduleData(
    val scheduleId: Int,
    val placeName: String,
    var positionPath: Int,
    val timeStamp: Long,
    val type: Int,
    val lat: Double,
    val lng: Double,
    val duration: Int,
)